package com.tcgsessionoverlay.session;

import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.util.EnumMap;
import java.util.Map;
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
	private final TcgStateReader tcgStateReader;
	private final Map<Skill, Long> sessionCarryBySkill = new EnumMap<>(Skill.class);
	private final Map<Skill, Long> sessionSkillXpBySkill = new EnumMap<>(Skill.class);

	private boolean sessionStarted;
	private boolean hasState;
	private long sessionCreditsEarned;

	@Inject
	public CreditsTracker(Client client, TcgStateReader tcgStateReader)
	{
		this.client = client;
		this.tcgStateReader = tcgStateReader;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		sessionStarted = false;
		sessionCarryBySkill.clear();
		sessionSkillXpBySkill.clear();
		hasState = false;
		sessionCreditsEarned = 0L;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!sessionStarted && SkillXp.isLoaded(client))
		{
			tcgStateReader.getState().ifPresent(this::startSession);
		}

		refreshSessionCredits();
	}

	private void refreshSessionCredits()
	{
		hasState = tcgStateReader.getState().isPresent();
		sessionCreditsEarned = hasState && sessionStarted ? creditsSinceSessionStart() : 0L;
	}

	private void startSession(TcgState saved)
	{
		for (Skill skill : Skill.values())
		{
			long currentSkillXp = client.getSkillExperience(skill);
			sessionSkillXpBySkill.put(skill, currentSkillXp);

			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits())
			{
				continue;
			}

			sessionCarryBySkill.put(skill, sessionCarry(saved, skill, currentSkillXp, rule));
		}

		sessionStarted = true;
	}

	private static long sessionCarry(TcgState saved, Skill skill, long currentSkillXp, CreditRule rule)
	{
		if (!saved.hasBaselineXp(skill))
		{
			return 0L;
		}

		return XpBlocks.xpIntoBlock(
			saved.getUncreditedXp(skill),
			saved.getBaselineXp(skill),
			currentSkillXp,
			rule.getXpPerBlock());
	}

	public boolean hasState()
	{
		return hasState;
	}

	public long getSessionCreditsEarned()
	{
		return sessionCreditsEarned;
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
