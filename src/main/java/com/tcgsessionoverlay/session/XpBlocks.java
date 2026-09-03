package com.tcgsessionoverlay.session;

public final class XpBlocks
{
	private XpBlocks()
	{
	}

	public static int xpIntoBlock(long anchorCarry, long anchorSkillXp, long currentSkillXp, int xpPerBlock)
	{
		if (xpPerBlock <= 0)
		{
			return 0;
		}

		return (int) Math.floorMod(accumulatedXp(anchorCarry, anchorSkillXp, currentSkillXp), (long) xpPerBlock);
	}

	public static int blocksCompleted(long anchorCarry, long anchorSkillXp, long currentSkillXp, int xpPerBlock)
	{
		if (xpPerBlock <= 0)
		{
			return 0;
		}

		long blocks = Math.floorDiv(accumulatedXp(anchorCarry, anchorSkillXp, currentSkillXp), (long) xpPerBlock);
		return (int) Math.max(0L, blocks);
	}

	private static long accumulatedXp(long anchorCarry, long anchorSkillXp, long currentSkillXp)
	{
		return anchorCarry + currentSkillXp - anchorSkillXp;
	}
}
