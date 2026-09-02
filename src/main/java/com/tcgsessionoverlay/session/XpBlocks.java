package com.tcgsessionoverlay.session;

public final class XpBlocks
{
	public static final int BLOCK_SIZE = 1000;

	private XpBlocks()
	{
	}

	public static int xpIntoBlock(long anchorCarry, long anchorSkillXp, long currentSkillXp)
	{
		return (int) Math.floorMod(accumulatedXp(anchorCarry, anchorSkillXp, currentSkillXp), (long) BLOCK_SIZE);
	}

	public static int blocksCompleted(long anchorCarry, long anchorSkillXp, long currentSkillXp)
	{
		long blocks = Math.floorDiv(accumulatedXp(anchorCarry, anchorSkillXp, currentSkillXp), (long) BLOCK_SIZE);
		return (int) Math.max(0L, blocks);
	}

	private static long accumulatedXp(long anchorCarry, long anchorSkillXp, long currentSkillXp)
	{
		return anchorCarry + currentSkillXp - anchorSkillXp;
	}
}
