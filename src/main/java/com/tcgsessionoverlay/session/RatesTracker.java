package com.tcgsessionoverlay.session;

import java.time.Duration;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RatesTracker
{
	static final Duration MINIMUM_ACTIVE_TIME = Duration.ofSeconds(60);
	private static final long MILLIS_PER_HOUR = Duration.ofHours(1).toMillis();

	private final CreditsTracker creditsTracker;
	private final SessionClock sessionClock;

	@Inject
	public RatesTracker(CreditsTracker creditsTracker, SessionClock sessionClock)
	{
		this.creditsTracker = creditsTracker;
		this.sessionClock = sessionClock;
	}

	public long getCreditsPerHour()
	{
		return perHour(creditsTracker.getSessionCreditsEarned(), sessionClock.getActiveTime());
	}

	static long perHour(long amount, Duration activeTime)
	{
		if (activeTime.compareTo(MINIMUM_ACTIVE_TIME) < 0)
		{
			return -1L;
		}

		return amount * MILLIS_PER_HOUR / activeTime.toMillis();
	}
}
