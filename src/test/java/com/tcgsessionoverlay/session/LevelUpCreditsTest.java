package com.tcgsessionoverlay.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LevelUpCreditsTest
{
	@Test
	public void matchesThePublishedCurve()
	{
		assertEquals(1250, LevelUpCredits.forLevel(1));
		assertEquals(1257, LevelUpCredits.forLevel(10));
		assertEquals(1307, LevelUpCredits.forLevel(20));
		assertEquals(1429, LevelUpCredits.forLevel(30));
		assertEquals(1667, LevelUpCredits.forLevel(40));
		assertEquals(2094, LevelUpCredits.forLevel(50));
		assertEquals(2862, LevelUpCredits.forLevel(60));
		assertEquals(4288, LevelUpCredits.forLevel(70));
		assertEquals(7101, LevelUpCredits.forLevel(80));
		assertEquals(13087, LevelUpCredits.forLevel(90));
		assertEquals(25000, LevelUpCredits.forLevel(99));
	}

	@Test
	public void matchesTheObservedFishingLevelUp()
	{
		assertEquals(2152, LevelUpCredits.forLevel(51));
	}

	@Test
	public void staysFlatAtTheFloorForTheFirstLevels()
	{
		assertEquals(1250, LevelUpCredits.forLevel(0));
		assertEquals(1250, LevelUpCredits.forLevel(2));
	}

	@Test
	public void capsFromNinetyNineUpwards()
	{
		assertEquals(25000, LevelUpCredits.forLevel(100));
		assertEquals(25000, LevelUpCredits.forLevel(126));
		assertEquals(25000, LevelUpCredits.forLevel(9999));
	}

	@Test
	public void increasesWithEveryLevel()
	{
		for (int level = 3; level < 99; level++)
		{
			assertEquals(level + " should award more than " + (level - 1),
				true,
				LevelUpCredits.forLevel(level) >= LevelUpCredits.forLevel(level - 1));
		}
	}
}
