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
	public static final int CREDITS_PER_BLOCK = 100;

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

			long currentSkillXp = client.getSkillExperience(skill);
			sessionCarryBySkill.put(skill, (long) XpBlocks.xpIntoBlock(
				saved.getUncreditedXp(skill),
				saved.getBaselineXp(skill),
				currentSkillXp));
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

		return state.get().getCredits() + (long) CREDITS_PER_BLOCK * blocksSinceSave(state.get());
	}

	public long getSessionCreditsEarned()
	{
		return (long) CREDITS_PER_BLOCK * blocksSinceSessionStart();
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

	private int blocksSinceSave(TcgState saved)
	{
		int blocks = 0;
		for (Skill skill : Skill.values())
		{
			if (saved.hasBaselineXp(skill))
			{
				blocks += XpBlocks.blocksCompleted(
					saved.getUncreditedXp(skill),
					saved.getBaselineXp(skill),
					client.getSkillExperience(skill));
			}
		}

		return blocks;
	}

	private int blocksSinceSessionStart()
	{
		int blocks = 0;
		for (Map.Entry<Skill, Long> entry : sessionSkillXpBySkill.entrySet())
		{
			blocks += XpBlocks.blocksCompleted(
				sessionCarryBySkill.get(entry.getKey()),
				entry.getValue(),
				client.getSkillExperience(entry.getKey()));
		}

		return blocks;
	}
}
