package com.tcgsessionoverlay.session;

import java.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class SessionClockIdleTest
{
	@Test
	public void isNotIdleBeforeAnyXpIsGained()
	{
		assertFalse(new SessionClock().isIdle());
	}

	@Test
	public void isNotIdleImmediatelyAfterAGain()
	{
		SessionClock clock = new SessionClock();
		clock.recordGain(System.nanoTime());

		assertFalse(clock.isIdle());
	}

	@Test
	public void treatsTheIdleThresholdAsFiveMinutes()
	{
		assertFalse(SessionClock.IDLE_THRESHOLD.minus(Duration.ofMinutes(5)).isNegative());
		assertFalse(Duration.ofMinutes(5).minus(SessionClock.IDLE_THRESHOLD).isNegative());
	}
}
