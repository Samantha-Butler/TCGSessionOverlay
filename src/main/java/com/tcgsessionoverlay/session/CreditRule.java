package com.tcgsessionoverlay.session;

import java.util.EnumSet;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.Skill;

@Getter
public enum CreditRule
{
	NONE(0, 0),
	STANDARD(1000, 100),
	SLAYER(100, 10);

	private static final Set<Skill> WITHOUT_XP_CREDITS = EnumSet.of(
		Skill.ATTACK,
		Skill.DEFENCE,
		Skill.STRENGTH,
		Skill.MAGIC,
		Skill.RANGED,
		Skill.HITPOINTS);

	private final int xpPerBlock;
	private final int creditsPerBlock;

	CreditRule(int xpPerBlock, int creditsPerBlock)
	{
		this.xpPerBlock = xpPerBlock;
		this.creditsPerBlock = creditsPerBlock;
	}

	public static CreditRule forSkill(Skill skill)
	{
		if (skill == null || WITHOUT_XP_CREDITS.contains(skill))
		{
			return NONE;
		}

		return skill == Skill.SLAYER ? SLAYER : STANDARD;
	}

	public boolean earnsCredits()
	{
		return this != NONE;
	}
}
