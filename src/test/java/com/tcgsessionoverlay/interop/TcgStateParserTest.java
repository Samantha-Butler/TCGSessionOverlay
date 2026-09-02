package com.tcgsessionoverlay.interop;

import java.util.Optional;
import net.runelite.api.Skill;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TcgStateParserTest
{
	private static final String SAMPLE = "{"
		+ "\"schemaVersion\":6,"
		+ "\"credits\":4640,"
		+ "\"openedPacks\":94,"
		+ "\"totalCreditsGained\":196040,"
		+ "\"profileSavedAtUnix\":1788382179,"
		+ "\"cardEntries\":["
		+ "{\"cardName\":\"Bronze bolts\",\"variants\":["
		+ "{\"id\":\"a\",\"pulledBy\":\"Player\",\"pulledAt\":1788264647888}]},"
		+ "{\"cardName\":\"Armadyl crozier\",\"variants\":["
		+ "{\"id\":\"b\",\"foil\":true,\"pulledBy\":\"Player\",\"pulledAt\":1788092514019,\"beta\":true},"
		+ "{\"id\":\"c\",\"pulledBy\":\"Player\",\"pulledAt\":1788092514020}]}],"
		+ "\"skillCreditBaseline\":{"
		+ "\"skillXp\":{\"Fishing\":101510,\"Cooking\":64960},"
		+ "\"uncreditedXpBySkill\":{\"Cooking\":890,\"Fishing\":500}}"
		+ "}";

	@Test
	public void parsesEconomyFields()
	{
		TcgState state = TcgStateParser.parse(SAMPLE).orElseThrow(AssertionError::new);

		assertEquals(4640L, state.getCredits());
		assertEquals(94L, state.getOpenedPacks());
		assertEquals(196040L, state.getTotalCreditsGained());
		assertEquals(1788382179L, state.getProfileSavedAtUnix());
	}

	@Test
	public void parsesCardsAndFlattensVariants()
	{
		TcgState state = TcgStateParser.parse(SAMPLE).orElseThrow(AssertionError::new);

		assertEquals(2, state.getUniqueCardNames());
		assertEquals(3, state.getOwnedCards().size());
		assertEquals(1, state.getFoilCount());
	}

	@Test
	public void treatsAbsentFoilAndBetaAsFalse()
	{
		TcgState state = TcgStateParser.parse(SAMPLE).orElseThrow(AssertionError::new);

		OwnedCard plainCard = state.getOwnedCards().get(0);
		assertEquals("Bronze bolts", plainCard.getCardName());
		assertEquals(1788264647888L, plainCard.getPulledAt());
		assertFalse(plainCard.isFoil());
		assertFalse(plainCard.isBeta());
	}

	@Test
	public void parsesUncreditedXpPerSkill()
	{
		TcgState state = TcgStateParser.parse(SAMPLE).orElseThrow(AssertionError::new);

		assertEquals(890L, state.getUncreditedXp(Skill.COOKING));
		assertEquals(500L, state.getUncreditedXp(Skill.FISHING));
		assertEquals(0L, state.getUncreditedXp(Skill.WOODCUTTING));
	}

	@Test
	public void parsesBaselineSkillXp()
	{
		TcgState state = TcgStateParser.parse(SAMPLE).orElseThrow(AssertionError::new);

		assertTrue(state.hasBaselineXp(Skill.FISHING));
		assertEquals(101510L, state.getBaselineXp(Skill.FISHING));
		assertEquals(64960L, state.getBaselineXp(Skill.COOKING));
		assertFalse(state.hasBaselineXp(Skill.WOODCUTTING));
		assertEquals(0L, state.getBaselineXp(Skill.WOODCUTTING));
	}

	@Test
	public void returnsEmptyForUnparsableInput()
	{
		assertFalse(TcgStateParser.parse(null).isPresent());
		assertFalse(TcgStateParser.parse("").isPresent());
		assertFalse(TcgStateParser.parse("not json").isPresent());
	}

	@Test
	public void toleratesMissingFields()
	{
		Optional<TcgState> parsed = TcgStateParser.parse("{\"credits\":10}");

		assertTrue(parsed.isPresent());
		assertEquals(10L, parsed.get().getCredits());
		assertEquals(0L, parsed.get().getOpenedPacks());
		assertTrue(parsed.get().getOwnedCards().isEmpty());
	}
}
