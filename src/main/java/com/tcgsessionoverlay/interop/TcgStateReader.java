package com.tcgsessionoverlay.interop;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Slf4j
@Singleton
public class TcgStateReader
{
	private static final String CONFIG_GROUP = "osrstcg";
	private static final String CONFIG_KEY = "state";
	private static final Duration CACHE_DURATION = Duration.ofSeconds(5);

	private final ConfigManager configManager;

	private Optional<String> cachedState = Optional.empty();
	private Instant cachedAt = Instant.MIN;

	@Inject
	public TcgStateReader(ConfigManager configManager)
	{
		this.configManager = configManager;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		cachedState = Optional.empty();
		cachedAt = Instant.MIN;
	}

	public Optional<String> getState()
	{
		Instant now = Instant.now();
		if (cachedState.isPresent() && Duration.between(cachedAt, now).compareTo(CACHE_DURATION) < 0)
		{
			return cachedState;
		}

		cachedState = readAndDecode();
		cachedAt = now;
		return cachedState;
	}

	private Optional<String> readAndDecode()
	{
		try
		{
			String raw = configManager.getRSProfileConfiguration(CONFIG_GROUP, CONFIG_KEY);
			if (raw == null)
			{
				return Optional.empty();
			}

			return TcgStateDecoder.decode(raw);
		}
		catch (Exception e)
		{
			log.debug("Failed to read osrs-tcg state", e);
			return Optional.empty();
		}
	}
}
