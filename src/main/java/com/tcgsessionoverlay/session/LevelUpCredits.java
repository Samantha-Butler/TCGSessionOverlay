package com.tcgsessionoverlay.session;

import net.runelite.api.Experience;

public final class LevelUpCredits
{
	static final int FLOOR = 1250;
	static final int CAP = 25000;
	private static final int PROGRESS_LEVELS = 97;
	private static final double CURVE_STEEPNESS = 2.5d;

	private LevelUpCredits()
	{
	}

	public static int forLevel(int level)
	{
		int clamped = clampLevel(level);
		if (clamped <= 2)
		{
			return FLOOR;
		}

		if (clamped >= Experience.MAX_REAL_LEVEL)
		{
			return CAP;
		}

		double progress = (clamped - 2.0d) / PROGRESS_LEVELS;
		double curve = Math.pow(progress, CURVE_STEEPNESS);
		double multiplier = Math.pow((double) CAP / FLOOR, curve);
		return (int) Math.round(FLOOR * multiplier);
	}

	public static long between(long fromSkillXp, long toSkillXp)
	{
		int fromLevel = levelForXp(fromSkillXp);
		int toLevel = levelForXp(toSkillXp);

		long total = 0L;
		for (int level = fromLevel + 1; level <= toLevel; level++)
		{
			total += forLevel(level);
		}

		return total;
	}

	static int levelForXp(long skillXp)
	{
		long bounded = Math.max(0L, Math.min(skillXp, Experience.MAX_SKILL_XP));
		return Experience.getLevelForXp((int) bounded);
	}

	private static int clampLevel(int level)
	{
		return level < 1 ? 1 : Math.min(level, Experience.MAX_VIRT_LEVEL);
	}
}
