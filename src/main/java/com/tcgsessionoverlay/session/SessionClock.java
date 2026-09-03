package com.tcgsessionoverlay.session;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import javax.inject.Singleton;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Singleton
public class SessionClock
{
	static final Duration IDLE_THRESHOLD = Duration.ofMinutes(5);

	private final Map<Skill, Integer> lastKnownXp = new EnumMap<>(Skill.class);

	private boolean hasRecordedGain;
	private long activeNanos;
	private long lastGainAtNanos;

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		lastKnownXp.clear();
		hasRecordedGain = false;
		activeNanos = 0;
		lastGainAtNanos = 0;
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		Skill skill = statChanged.getSkill();
		int currentXp = statChanged.getXp();
		Integer previousXp = lastKnownXp.put(skill, currentXp);

		if (previousXp != null && currentXp > previousXp)
		{
			recordGain(System.nanoTime());
		}
	}

	void recordGain(long nowNanos)
	{
		if (hasRecordedGain)
		{
			activeNanos += activeGap(lastGainAtNanos, nowNanos);
		}

		hasRecordedGain = true;
		lastGainAtNanos = nowNanos;
	}

	static long activeGap(long fromNanos, long toNanos)
	{
		long gap = toNanos - fromNanos;
		return gap > 0 && gap <= IDLE_THRESHOLD.toNanos() ? gap : 0L;
	}

	public Duration getActiveTime()
	{
		return Duration.ofNanos(activeNanos);
	}
}
