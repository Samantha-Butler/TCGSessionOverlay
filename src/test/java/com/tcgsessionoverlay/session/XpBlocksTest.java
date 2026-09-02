package com.tcgsessionoverlay.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class XpBlocksTest
{
	@Test
	public void usesAnchorCarryWhenNoXpGainedSinceAnchor()
	{
		assertEquals(500, XpBlocks.xpIntoBlock(500L, 101510L, 101510L));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101510L));
	}

	@Test
	public void addsXpGainedSinceAnchorToAnchorCarry()
	{
		assertEquals(560, XpBlocks.xpIntoBlock(500L, 101510L, 101570L));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101570L));
	}

	@Test
	public void wrapsAndCountsOneCompletedBlock()
	{
		assertEquals(120, XpBlocks.xpIntoBlock(890L, 64960L, 65190L));
		assertEquals(1, XpBlocks.blocksCompleted(890L, 64960L, 65190L));
	}

	@Test
	public void countsSeveralCompletedBlocks()
	{
		assertEquals(200, XpBlocks.xpIntoBlock(900L, 1000L, 4300L));
		assertEquals(4, XpBlocks.blocksCompleted(900L, 1000L, 4300L));
	}

	@Test
	public void matchesTheObservedFishingSession()
	{
		assertEquals(580, XpBlocks.xpIntoBlock(500L, 101510L, 105590L));
		assertEquals(4, XpBlocks.blocksCompleted(500L, 101510L, 105590L));
	}

	@Test
	public void staysInRangeWhenCurrentXpIsBehindTheAnchor()
	{
		assertEquals(400, XpBlocks.xpIntoBlock(500L, 101510L, 101410L));
		assertEquals(0, XpBlocks.blocksCompleted(500L, 101510L, 101410L));
	}
}
