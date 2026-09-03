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
	private long sessionStartPacks;

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
		sessionStartPacks = 0;
		sessionCarryBySkill.clear();
		sessionSkillXpBySkill.clear();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!sessionStarted)
		{
			tcgStateReader.getState().ifPresent(this::startSession);
		}
	}

	private void startSession(TcgState saved)
	{
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

			long currentSkillXp = client.getSkillExperience(skill);
			sessionCarryBySkill.put(skill, (long) XpBlocks.xpIntoBlock(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				currentSkillXp,
				rule.getXpPerBlock()));
			sessionSkillXpBySkill.put(skill, currentSkillXp);
		}

		sessionStartPacks = saved.getOpenedPacks();
		sessionStarted = true;
	}

	public boolean hasState()
	{
		return tcgStateReader.getState().isPresent();
	}

	public long getCredits()
	{
		Optional<TcgState> state = tcgStateReader.getState();
		if (!state.isPresent())
		{
			return 0L;
		}

		return state.get().getCredits() + creditsSinceSave(state.get());
	}

	public long getSessionCreditsEarned()
	{
		return creditsSinceSessionStart();
	}

	public long getOpenedPacks()
	{
		return tcgStateReader.getState().map(TcgState::getOpenedPacks).orElse(0L);
	}

	public long getSessionPacksOpened()
	{
		return sessionStarted ? getOpenedPacks() - sessionStartPacks : 0L;
	}

	public int getPackCost()
	{
		return Math.max(1, config.packCost());
	}

	public long getPacksAffordable()
	{
		return Math.max(0L, getCredits()) / getPackCost();
	}

	public long getCreditsTowardNextPack()
	{
		return Math.max(0L, getCredits()) % getPackCost();
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
			CreditRule rule = CreditRule.forSkill(skill);
			if (!rule.earnsCredits() || !saved.hasBaselineXp(skill))
			{
				continue;
			}

			credits += (long) rule.getCreditsPerBlock() * XpBlocks.blocksCompleted(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				client.getSkillExperience(skill),
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
			CreditRule rule = CreditRule.forSkill(skill);

			credits += (long) rule.getCreditsPerBlock() * XpBlocks.blocksCompleted(
				sessionCarryBySkill.get(skill),
				entry.getValue(),
				client.getSkillExperience(skill),
				rule.getXpPerBlock());
		}

		return credits;
	}
}
