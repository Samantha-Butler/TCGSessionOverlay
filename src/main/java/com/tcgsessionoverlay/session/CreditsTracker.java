package com.tcgsessionoverlay.session;

import com.tcgsessionoverlay.TcgSessionOverlayConfig;
import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
public class CreditsTracker
{
	private final Client client;
	private final TcgSessionOverlayConfig config;
	private final TcgStateReader tcgStateReader;
	private final Map<Skill, Long> sessionCarryBySkill = new EnumMap<>(Skill.class);
	private final Map<Skill, Long> sessionSkillXpBySkill = new EnumMap<>(Skill.class);

	private boolean sessionStarted;
	private boolean hasState;
	private long credits;
	private long lifetimeCredits;
	private long sessionCreditsEarned;

	@Inject
	public CreditsTracker(Client client, TcgSessionOverlayConfig config, TcgStateReader tcgStateReader)
	{
		this.client = client;
		this.config = config;
		this.tcgStateReader = tcgStateReader;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		sessionStarted = false;
		sessionCarryBySkill.clear();
		sessionSkillXpBySkill.clear();
		clearTotals();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!sessionStarted)
		{
			tcgStateReader.getState().ifPresent(this::startSession);
		}

		refreshTotals();
	}

	private void refreshTotals()
	{
		Optional<TcgState> state = tcgStateReader.getState();
		if (!state.isPresent())
		{
			clearTotals();
			return;
		}

		TcgState saved = state.get();
		hasState = true;
		long earnedSinceSave = creditsSinceSave(saved);
		credits = saved.getCredits() + earnedSinceSave;
		lifetimeCredits = saved.getTotalCreditsGained() + earnedSinceSave;
		sessionCreditsEarned = sessionStarted ? creditsSinceSessionStart() : 0L;
	}

	private void clearTotals()
	{
		hasState = false;
		credits = 0L;
		lifetimeCredits = 0L;
		sessionCreditsEarned = 0L;
	}

	private void startSession(TcgState saved)
	{
		for (Skill skill : Skill.values())
		{
			if (!saved.hasBaselineXp(skill))
			{
				continue;
			}

			long currentSkillXp = client.getSkillExperience(skill);
			sessionSkillXpBySkill.put(skill, currentSkillXp);

			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits())
			{
				continue;
			}

			sessionCarryBySkill.put(skill, (long) XpBlocks.xpIntoBlock(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				currentSkillXp,
				rule.getXpPerBlock()));
		}

		sessionStarted = true;
	}

	public boolean hasState()
	{
		return hasState;
	}

	public long getCredits()
	{
		return credits;
	}

	public long getLifetimeCredits()
	{
		return lifetimeCredits;
	}

	public long getSessionCreditsEarned()
	{
		return sessionCreditsEarned;
	}

	public int getPackCost()
	{
		return Math.max(1, config.packCost());
	}

	public long getPacksAffordable()
	{
		return Math.max(0L, credits) / getPackCost();
	}

	public long getCreditsTowardNextPack()
	{
		return Math.max(0L, credits) % getPackCost();
	}

	public long getCreditsToNextPack()
	{
		return getPackCost() - getCreditsTowardNextPack();
	}

	private long creditsSinceSave(TcgState saved)
	{
		long credits = 0L;
		for (Skill skill : Skill.values())
		{
			if (!saved.hasBaselineXp(skill))
			{
				continue;
			}

			long currentSkillXp = client.getSkillExperience(skill);
			credits += LevelUpCredits.between(saved.getBaselineXp(skill), currentSkillXp);

			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits())
			{
				continue;
			}

			credits += (long) rule.getCreditsPerBlock() * XpBlocks.blocksCompleted(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				currentSkillXp,
				rule.getXpPerBlock());
		}

		return credits;
	}

	private long creditsSinceSessionStart()
	{
		long credits = 0L;
		for (Map.Entry<Skill, Long> entry : sessionSkillXpBySkill.entrySet())
		{
			Skill skill = entry.getKey();
			long currentSkillXp = client.getSkillExperience(skill);
			credits += LevelUpCredits.between(entry.getValue(), currentSkillXp);

			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits())
			{
				continue;
			}

			credits += (long) rule.getCreditsPerBlock() * XpBlocks.blocksCompleted(
				sessionCarryBySkill.get(skill),
				entry.getValue(),
				currentSkillXp,
				rule.getXpPerBlock());
		}

		return credits;
	}
}
