package com.tcgsessionoverlay.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XpCountdownTrackerTest
{
	@Test
	public void usesSavedCarryWhenNoXpGainedSinceSave()
	{
		assertEquals(500, XpCountdownTracker.anchoredBlockXp(500L, 101510L, 101510L));
	}

	@Test
	public void addsXpGainedSinceSaveToSavedCarry()
	{
		assertEquals(560, XpCountdownTracker.anchoredBlockXp(500L, 101510L, 101570L));
	}

	@Test
	public void wrapsWhenXpSinceSaveCrossesABlock()
	{
		assertEquals(120, XpCountdownTracker.anchoredBlockXp(890L, 64960L, 65190L));
	}

	@Test
	public void wrapsWhenXpSinceSaveCrossesSeveralBlocks()
	{
		assertEquals(200, XpCountdownTracker.anchoredBlockXp(900L, 1000L, 4300L));
	}

	@Test
	public void staysInRangeWhenCurrentXpIsBehindTheSave()
	{
		assertEquals(400, XpCountdownTracker.anchoredBlockXp(500L, 101510L, 101410L));
	}
}
