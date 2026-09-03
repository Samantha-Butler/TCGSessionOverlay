package com.tcgsessionoverlay.session;

import net.runelite.api.Skill;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CreditRuleTest
{
	@Test
	public void combatSkillsEarnNoXpCredits()
	{
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.ATTACK));
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.DEFENCE));
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.STRENGTH));
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.MAGIC));
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.RANGED));
	}

	@Test
	public void hitpointsEarnsNoXpCredits()
	{
		assertEquals(CreditRule.NONE, CreditRule.forSkill(Skill.HITPOINTS));
		assertFalse(CreditRule.forSkill(Skill.HITPOINTS).earnsCredits());
	}

	@Test
	public void slayerUsesTheSmallerChunk()
	{
		CreditRule rule = CreditRule.forSkill(Skill.SLAYER);

		assertEquals(CreditRule.SLAYER, rule);
		assertEquals(100, rule.getXpPerBlock());
		assertEquals(10, rule.getCreditsPerBlock());
	}

	@Test
	public void everyOtherSkillUsesTheStandardChunk()
	{
		CreditRule rule = CreditRule.forSkill(Skill.FISHING);

		assertEquals(CreditRule.STANDARD, rule);
		assertEquals(1000, rule.getXpPerBlock());
		assertEquals(100, rule.getCreditsPerBlock());
		assertTrue(rule.earnsCredits());
	}

	@Test
	public void slayerAndStandardPayTheSameRatePerXp()
	{
		assertEquals(
			CreditRule.STANDARD.getXpPerBlock() / CreditRule.STANDARD.getCreditsPerBlock(),
			CreditRule.SLAYER.getXpPerBlock() / CreditRule.SLAYER.getCreditsPerBlock());
	}
}
