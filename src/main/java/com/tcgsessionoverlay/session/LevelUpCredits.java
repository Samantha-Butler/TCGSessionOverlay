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

	private static int clampLevel(int level)
	{
		return level < 1 ? 1 : Math.min(level, Experience.MAX_VIRT_LEVEL);
	}
}
