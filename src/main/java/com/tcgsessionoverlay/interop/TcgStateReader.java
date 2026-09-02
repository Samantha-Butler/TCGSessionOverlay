package com.tcgsessionoverlay.interop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.RuneScapeProfileChanged;

@Slf4j
@Singleton
public class TcgStateReader
{
	private static final String SAVE_FILE_NAME = "tcg.save";
	private static final Duration CACHE_DURATION = Duration.ofSeconds(5);

	private final Client client;

	private Optional<String> cachedState = Optional.empty();
	private Instant cachedAt = Instant.MIN;

	@Inject
	public TcgStateReader(Client client)
	{
		this.client = client;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		cachedState = Optional.empty();
		cachedAt = Instant.MIN;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		getState();
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
		Path saveFile = resolveSaveFile();
		if (saveFile == null || !Files.isRegularFile(saveFile))
		{
			log.debug("No osrs-tcg save file found for this account");
			return Optional.empty();
		}

		try
		{
			String raw = Files.readString(saveFile, StandardCharsets.UTF_8);
			Optional<String> decoded = TcgStateDecoder.decode(raw);
			if (decoded.isPresent())
			{
				log.debug("Decoded osrs-tcg state: {}", decoded.get());
			}
			else
			{
				log.debug("osrs-tcg save file was present but could not be decoded");
			}

			return decoded;
		}
		catch (IOException e)
		{
			log.debug("Failed to read osrs-tcg save file", e);
			return Optional.empty();
		}
	}

	private Path resolveSaveFile()
	{
		long accountHash = client.getAccountHash();
		if (accountHash == -1L)
		{
			return null;
		}

		String accountDirName = sha256Hex(Long.toString(accountHash));
		return Path.of(RuneLite.RUNELITE_DIR.getAbsolutePath(), "OSRS-TCG", "profiles", accountDirName, SAVE_FILE_NAME);
	}

	private static String sha256Hex(String value)
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(hash.length * 2);
			for (byte b : hash)
			{
				builder.append(String.format("%02x", b));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
