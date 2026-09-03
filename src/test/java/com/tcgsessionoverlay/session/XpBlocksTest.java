package com.tcgsessionoverlay.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XpBlocksTest
{
	private static final int STANDARD = 1000;
	private static final int SLAYER = 100;

	@Test
	public void usesAnchorCarryWhenNoXpGainedSinceAnchor()
	{
		assertEquals(500, XpBlocks.xpIntoBlock(500L, 101510L, 101510L, STANDARD));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101510L, STANDARD));
	}

	@Test
	public void addsXpGainedSinceAnchorToAnchorCarry()
	{
		assertEquals(560, XpBlocks.xpIntoBlock(500L, 101510L, 101570L, STANDARD));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101570L, STANDARD));
	}

	@Test
	public void wrapsAndCountsOneCompletedBlock()
	{
		assertEquals(120, XpBlocks.xpIntoBlock(890L, 64960L, 65190L, STANDARD));
		assertEquals(1, XpBlocks.blocksCompleted(890L, 64960L, 65190L, STANDARD));
	}

	@Test
	public void countsSeveralCompletedBlocks()
	{
		assertEquals(200, XpBlocks.xpIntoBlock(900L, 1000L, 4300L, STANDARD));
		assertEquals(4, XpBlocks.blocksCompleted(900L, 1000L, 4300L, STANDARD));
	}

	@Test
	public void matchesTheObservedFishingSession()
	{
		assertEquals(580, XpBlocks.xpIntoBlock(500L, 101510L, 105590L, STANDARD));
		assertEquals(4, XpBlocks.blocksCompleted(500L, 101510L, 105590L, STANDARD));
	}

	@Test
	public void countsSlayerInHundredXpBlocks()
	{
		assertEquals(50, XpBlocks.xpIntoBlock(0L, 1000L, 1350L, SLAYER));
		assertEquals(3, XpBlocks.blocksCompleted(0L, 1000L, 1350L, SLAYER));
	}

	@Test
	public void staysInRangeWhenCurrentXpIsBehindTheAnchor()
	{
		assertEquals(400, XpBlocks.xpIntoBlock(500L, 101510L, 101410L, STANDARD));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101410L, STANDARD));
	}

	@Test
	public void yieldsNothingForASkillThatEarnsNoCredits()
	{
		assertEquals(0, XpBlocks.xpIntoBlock(0L, 1000L, 9999L, 0));
		assertEquals(0, XpBlocks.blocksCompleted(0L, 1000L, 9999L, 0));
	}
}
