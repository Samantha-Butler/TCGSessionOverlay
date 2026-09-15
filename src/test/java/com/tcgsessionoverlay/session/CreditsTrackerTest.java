package com.tcgsessionoverlay.session;

import net.runelite.api.Skill;
import net.runelite.api.events.GameTick;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CreditsTrackerTest
{
	private final FakeClient game = new FakeClient();
	private final CreditsTracker tracker = new CreditsTracker(game.client(), null, new SavedStateReader(game.client()));

	@Test
	public void startsTheSessionAtZeroWhenSkillXpArrivesAfterTheSave()
	{
		tracker.onGameTick(new GameTick());
		loadSavedSkillXp();
		tracker.onGameTick(new GameTick());

		assertEquals(0L, tracker.getSessionCreditsEarned());
	}

	@Test
	public void countsCreditsEarnedAfterSkillXpArrives()
	{
		tracker.onGameTick(new GameTick());
		loadSavedSkillXp();
		tracker.onGameTick(new GameTick());
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP + 700);
		tracker.onGameTick(new GameTick());

		assertEquals(100L, tracker.getSessionCreditsEarned());
		assertEquals(SavedStateReader.CREDITS + 100, tracker.getCredits());
	}

	private void loadSavedSkillXp()
	{
		game.setXp(Skill.HITPOINTS, SavedStateReader.HITPOINTS_XP);
		game.setXp(Skill.FIREMAKING, SavedStateReader.FIREMAKING_XP);
	}
}
