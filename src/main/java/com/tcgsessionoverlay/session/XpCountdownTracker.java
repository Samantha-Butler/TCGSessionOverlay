package com.tcgsessionoverlay.session;

import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.time.Duration;
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
import net.runelite.api.Experience;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
public class XpCountdownTracker
{
	private static final int SAMPLE_WINDOW = 10;
	private static final Duration PREFERRED_SKILL_WINDOW = Duration.ofMinutes(5);

	private final Client client;
	private final TcgStateReader tcgStateReader;
	private final Map<Skill, Integer> lastKnownXp = new EnumMap<>(Skill.class);
	private final Map<Skill, Integer> xpInBlockBySkill = new EnumMap<>(Skill.class);
	private final Deque<Integer> recentActionXp = new ArrayDeque<>();

	private Skill trackedSkill;
	private long trackedSkillLastGainAtNanos;
	private long anchoredSaveTime;

	@Inject
	public XpCountdownTracker(Client client, TcgStateReader tcgStateReader)
	{
		this.client = client;
		this.tcgStateReader = tcgStateReader;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		lastKnownXp.clear();
		recentActionXp.clear();
		trackedSkill = null;
		trackedSkillLastGainAtNanos = 0;
		anchoredSaveTime = 0;
		xpInBlockBySkill.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		anchorToSavedState();
	}

	private void anchorToSavedState()
	{
		if (!SkillXp.isLoaded(client))
		{
			return;
		}

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

			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits())
			{
				continue;
			}

			xpInBlockBySkill.put(skill, XpBlocks.xpIntoBlock(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				client.getSkillExperience(skill),
				rule.getXpPerBlock()));
		}
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

		trackDisplayedSkill(skill, gained, System.nanoTime());

		CreditRule rule = CreditRule.forSkill(skill);
		if (!rule.earnsCredits())
		{
			return;
		}

		int updatedBlockXp = (xpInBlockBySkill.getOrDefault(skill, 0) + gained) % rule.getXpPerBlock();
		xpInBlockBySkill.put(skill, updatedBlockXp);
	}

	void trackDisplayedSkill(Skill skill, int gained, long nowNanos)
	{
		if (!shouldDisplay(skill, nowNanos))
		{
			return;
		}

		if (skill != trackedSkill)
		{
			trackedSkill = skill;
			recentActionXp.clear();
		}

		trackedSkillLastGainAtNanos = nowNanos;
		recentActionXp.addLast(gained);
		if (recentActionXp.size() > SAMPLE_WINDOW)
		{
			recentActionXp.removeFirst();
		}
	}

	private boolean shouldDisplay(Skill skill, long nowNanos)
	{
		if (trackedSkill == null || skill == trackedSkill)
		{
			return true;
		}

		if (displayPriority(skill) >= displayPriority(trackedSkill))
		{
			return true;
		}

		return nowNanos - trackedSkillLastGainAtNanos > PREFERRED_SKILL_WINDOW.toNanos();
	}

	static int displayPriority(Skill skill)
	{
		if (CreditRule.forSkill(skill).earnsCredits())
		{
			return 2;
		}

		return skill == Skill.HITPOINTS ? 0 : 1;
	}

	public int getXpInCurrentBlock()
	{
		if (trackedSkill == null)
		{
			return 0;
		}

		return xpInBlockBySkill.getOrDefault(trackedSkill, 0);
	}

	public int getBlockSize()
	{
		return CreditRule.forSkill(trackedSkill).getXpPerBlock();
	}

	public boolean isTrackedSkillEarningCredits()
	{
		return CreditRule.forSkill(trackedSkill).earnsCredits();
	}

	public int getNextLevelCredits()
	{
		if (trackedSkill == null)
		{
			return 0;
		}

		return LevelUpCredits.forLevel(Experience.getLevelForXp(client.getSkillExperience(trackedSkill)) + 1);
	}

	public int getXpRemainingInBlock()
	{
		return getBlockSize() - getXpInCurrentBlock();
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
		if (medianXp <= 0 || getBlockSize() <= 0)
		{
			return -1;
		}

		return (int) Math.ceil(getXpRemainingInBlock() / (double) medianXp);
	}
}
