package com.tcgsessionoverlay.session;

import java.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RatesTrackerTest
{
	@Test
	public void reportsUnknownBelowTheMinimumActiveTime()
	{
		assertEquals(-1L, RatesTracker.perHour(500L, Duration.ofSeconds(59)));
	}

	@Test
	public void scalesAnHourOfCreditsDirectly()
	{
		assertEquals(1240L, RatesTracker.perHour(1240L, Duration.ofHours(1)));
	}

	@Test
	public void scalesUpFromAShortSession()
	{
		assertEquals(1200L, RatesTracker.perHour(300L, Duration.ofMinutes(15)));
	}

	@Test
	public void reportsZeroWhenNothingWasEarned()
	{
		assertEquals(0L, RatesTracker.perHour(0L, Duration.ofHours(2)));
	}
}
