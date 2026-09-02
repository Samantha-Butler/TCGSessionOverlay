package com.tcgsessionoverlay.session;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
public class XpCountdownTracker
{
	private static final int BLOCK_SIZE = 1000;
	private static final int SAMPLE_WINDOW = 10;
	private static final String CONFIG_GROUP = "tcgsessionoverlay";
	private static final String XP_IN_BLOCK_KEY = "xpInCurrentBlock";

	private final ConfigManager configManager;
	private final Map<Skill, Integer> lastKnownXp = new EnumMap<>(Skill.class);
	private final Deque<Integer> recentActionXp = new ArrayDeque<>();

	private int xpInCurrentBlock;
	private Skill trackedSkill;

	@Inject
	public XpCountdownTracker(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		loadState();
	}

	private void loadState()
	{
		Integer saved = configManager.getRSProfileConfiguration(CONFIG_GROUP, XP_IN_BLOCK_KEY, Integer.class);
		if (saved != null)
		{
			xpInCurrentBlock = saved;
		}
	}

	private void saveState()
	{
		configManager.setRSProfileConfiguration(CONFIG_GROUP, XP_IN_BLOCK_KEY, xpInCurrentBlock);
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

		xpInCurrentBlock = (xpInCurrentBlock + gained) % BLOCK_SIZE;
		saveState();
	}

	public int getXpInCurrentBlock()
	{
		return xpInCurrentBlock;
	}

	public int getXpRemainingInBlock()
	{
		return BLOCK_SIZE - xpInCurrentBlock;
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
