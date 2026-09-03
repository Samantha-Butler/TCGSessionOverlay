package com.tcgsessionoverlay.session;

import java.time.Duration;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SessionClockTest
{
	private static final long SECOND = Duration.ofSeconds(1).toNanos();
	private static final long MINUTE = Duration.ofMinutes(1).toNanos();

	@Test
	public void countsGapsShorterThanTheIdleThreshold()
	{
		assertEquals(3 * SECOND, SessionClock.activeGap(0L, 3 * SECOND));
	}

	@Test
	public void countsAGapExactlyAtTheIdleThreshold()
	{
		assertEquals(5 * MINUTE, SessionClock.activeGap(0L, 5 * MINUTE));
	}

	@Test
	public void ignoresGapsLongerThanTheIdleThreshold()
	{
		assertEquals(0L, SessionClock.activeGap(0L, 20 * MINUTE));
	}

	@Test
	public void accumulatesOnlyActiveGaps()
	{
		SessionClock clock = new SessionClock();
		clock.recordGain(0L);
		clock.recordGain(3 * SECOND);
		clock.recordGain(6 * SECOND);
		clock.recordGain(6 * SECOND + 20 * MINUTE);
		clock.recordGain(6 * SECOND + 20 * MINUTE + 4 * SECOND);

		assertEquals(Duration.ofSeconds(10), clock.getActiveTime());
	}

	@Test
	public void startsAtZeroBeforeAnySecondGain()
	{
		SessionClock clock = new SessionClock();
		clock.recordGain(0L);

		assertEquals(Duration.ZERO, clock.getActiveTime());
	}
}
