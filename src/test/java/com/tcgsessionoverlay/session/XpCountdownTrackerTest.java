package com.tcgsessionoverlay.session;

import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XpCountdownTrackerTest
{
	@Test
	public void anchorsBlockProgressToTheSaveOnceSkillXpArrives()
	{
		FakeClient game = new FakeClient();
		XpCountdownTracker tracker = new XpCountdownTracker(game.client(), SavedStateReader.firemakingSave(game.client()));

		tracker.onGameTick(new GameTick());
		game.setXp(Skill.HITPOINTS, SavedStateReader.HITPOINTS_XP);
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP + 200);
		tracker.onGameTick(new GameTick());
		tracker.trackDisplayedSkill(Skill.FIREMAKING, 200, 0L);

		assertEquals(SavedStateReader.FIREMAKING_CARRY + 200, tracker.getXpInCurrentBlock());
	}

	@Test
	public void ranksCreditEarningSkillsHighest()
	{
		assertEquals(2, XpCountdownTracker.displayPriority(Skill.SLAYER));
		assertEquals(2, XpCountdownTracker.displayPriority(Skill.FISHING));
	}

	@Test
	public void ranksAttackStyleSkillsAboveHitpoints()
	{
		assertTrue(XpCountdownTracker.displayPriority(Skill.ATTACK)
			> XpCountdownTracker.displayPriority(Skill.HITPOINTS));
		assertTrue(XpCountdownTracker.displayPriority(Skill.RANGED)
			> XpCountdownTracker.displayPriority(Skill.HITPOINTS));
		assertTrue(XpCountdownTracker.displayPriority(Skill.MAGIC)
			> XpCountdownTracker.displayPriority(Skill.HITPOINTS));
	}

	@Test
	public void ranksSlayerAboveTheCombatSkillsItIsTrainedWith()
	{
		assertTrue(XpCountdownTracker.displayPriority(Skill.SLAYER)
			> XpCountdownTracker.displayPriority(Skill.ATTACK));
		assertTrue(XpCountdownTracker.displayPriority(Skill.SLAYER)
			> XpCountdownTracker.displayPriority(Skill.HITPOINTS));
	}

	@Test
	public void ranksHitpointsLast()
	{
		for (Skill skill : Skill.values())
		{
			assertTrue(skill.getName(), XpCountdownTracker.displayPriority(skill)
				>= XpCountdownTracker.displayPriority(Skill.HITPOINTS));
		}
	}
}
