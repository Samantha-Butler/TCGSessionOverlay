package com.tcgsessionoverlay.session;

import com.google.gson.reflect.TypeToken;
import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
public class XpCountdownTracker
{
	private static final int SAMPLE_WINDOW = 10;
	private static final String CONFIG_GROUP = "tcgsessionoverlay";
	private static final String XP_IN_BLOCK_KEY = "xpInBlockBySkill";
	private static final Type XP_IN_BLOCK_TYPE = new TypeToken<Map<Skill, Integer>>()
	{
	}.getType();

	private final Client client;
	private final ConfigManager configManager;
	private final TcgStateReader tcgStateReader;
	private final Map<Skill, Integer> lastKnownXp = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> xpInBlockBySkill = new EnumMap<>(Skill.class);
	private final Deque<Integer> recentActionXp = new ArrayDeque<>();

	private Skill trackedSkill;
	private long anchoredSaveTime;

	@Inject
	public XpCountdownTracker(Client client, ConfigManager configManager, TcgStateReader tcgStateReader)
	{
		this.client = client;
		this.configManager = configManager;
		this.tcgStateReader = tcgStateReader;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		anchoredSaveTime = 0;
		loadState();
	}

	private void loadState()
	{
		Map<Skill, Integer> saved = configManager.getRSProfileConfiguration(CONFIG_GROUP, XP_IN_BLOCK_KEY, XP_IN_BLOCK_TYPE);
		xpInBlockBySkill.clear();
		if (saved != null)
		{
			xpInBlockBySkill.putAll(saved);
		}
	}

	private void saveState()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, XP_IN_BLOCK_KEY, xpInBlockBySkill);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		anchorToSavedState();
	}

	private void anchorToSavedState()
	{
		Optional<TcgState> state = tcgStateReader.getState();
		if (!state.isPresent())
		{
			return;
		}

		TcgState saved = state.get();
		if (saved.getProfileSavedAtUnix() == anchoredSaveTime)
		{
			return;
		}

		anchoredSaveTime = saved.getProfileSavedAtUnix();

		for (Skill skill : Skill.values())
		{
			if (!saved.hasBaselineXp(skill))
			{
				continue;
			}

			xpInBlockBySkill.put(skill, XpBlocks.xpIntoBlock(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				client.getSkillExperience(skill)));
		}

		saveState();
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();
		int currentXp = statChanged.getXp();
		Integer previousXp = lastKnownXp.put(skill, currentXp);

		if (previousXp == null)
		{
			return;
		}

		int gained = currentXp - previousXp;
		if (gained <= 0)
		{
			return;
		}

		if (skill != trackedSkill)
		{
			trackedSkill = skill;
			recentActionXp.clear();
		}

		recentActionXp.addLast(gained);
		if (recentActionXp.size() > SAMPLE_WINDOW)
		{
			recentActionXp.removeFirst();
		}

		int updatedBlockXp = (xpInBlockBySkill.getOrDefault(skill, 0) + gained) % XpBlocks.BLOCK_SIZE;
		xpInBlockBySkill.put(skill, updatedBlockXp);
		saveState();
	}

	public int getXpInCurrentBlock()
	{
		if (trackedSkill == null)
		{
			return 0;
		}

		return xpInBlockBySkill.getOrDefault(trackedSkill, 0);
	}

	public int getXpRemainingInBlock()
	{
		return XpBlocks.BLOCK_SIZE - getXpInCurrentBlock();
	}

	public Skill getTrackedSkill()
	{
		return trackedSkill;
	}

	public int getMedianXpPerAction()
	{
		if (recentActionXp.isEmpty())
		{
			return 0;
		}

		List<Integer> sorted = new ArrayList<>(recentActionXp);
		Collections.sort(sorted);

		int middle = sorted.size() / 2;
		if (sorted.size() % 2 == 0)
		{
			return (sorted.get(middle - 1) + sorted.get(middle)) / 2;
		}

		return sorted.get(middle);
	}

	public int getActionsRemaining()
	{
		int medianXp = getMedianXpPerAction();
		if (medianXp <= 0)
		{
			return -1;
		}

		return (int) Math.ceil(getXpRemainingInBlock() / (double) medianXp);
	}
}
