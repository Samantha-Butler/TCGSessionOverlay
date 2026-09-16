package com.tcgsessionoverlay.session;

import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CreditsTrackerTest
{
	private final FakeClient game = new FakeClient();

	@Test
	public void startsTheSessionAtZeroWhenSkillXpArrivesAfterTheSave()
	{
		CreditsTracker tracker = trackerWith(SavedStateReader.firemakingSave(game.client()));

		tracker.onGameTick(new GameTick());
		loadSavedSkillXp();
		tracker.onGameTick(new GameTick());

		assertEquals(0L, tracker.getSessionCreditsEarned());
	}

	@Test
	public void countsCreditsEarnedAfterSkillXpArrives()
	{
		CreditsTracker tracker = trackerWith(SavedStateReader.firemakingSave(game.client()));

		tracker.onGameTick(new GameTick());
		loadSavedSkillXp();
		tracker.onGameTick(new GameTick());
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP + 700);
		tracker.onGameTick(new GameTick());

		assertEquals(100L, tracker.getSessionCreditsEarned());
		assertTrue(tracker.hasBalance());
		assertEquals(SavedStateReader.CREDITS + 100, tracker.getCredits());
	}

	@Test
	public void countsSessionCreditsWithoutABalanceWhenTheSaveHasNoSkillXp()
	{
		CreditsTracker tracker = trackerWith(SavedStateReader.emptySave(game.client()));

		loadSavedSkillXp();
		tracker.onGameTick(new GameTick());
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP + 1000);
		tracker.onGameTick(new GameTick());

		assertEquals(100L, tracker.getSessionCreditsEarned());
		assertFalse(tracker.hasBalance());
	}

	private CreditsTracker trackerWith(SavedStateReader reader)
	{
		return new CreditsTracker(game.client(), null, reader);
	}

	private void loadSavedSkillXp()
	{
		game.setXp(Skill.HITPOINTS, SavedStateReader.HITPOINTS_XP);
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP);
	}
}
