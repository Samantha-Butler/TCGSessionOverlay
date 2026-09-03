package com.tcgsessionoverlay.session;

import net.runelite.api.Skill;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class XpCountdownTrackerTest
{
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
