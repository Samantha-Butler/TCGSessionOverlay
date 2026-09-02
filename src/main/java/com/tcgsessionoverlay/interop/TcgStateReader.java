package com.tcgsessionoverlay.interop;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
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
	private static final Duration CHECK_INTERVAL = Duration.ofSeconds(5);

	private final Client client;

	private Optional<TcgState> cachedState = Optional.empty();
	private Instant lastCheckedAt = Instant.MIN;
	private FileTime lastModified;

	@Inject
	public TcgStateReader(Client client)
	{
		this.client = client;
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		cachedState = Optional.empty();
		lastCheckedAt = Instant.MIN;
		lastModified = null;
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		getState();
	}

	public Optional<TcgState> getState()
	{
		Instant now = Instant.now();
		if (Duration.between(lastCheckedAt, now).compareTo(CHECK_INTERVAL) < 0)
		{
			return cachedState;
		}

		lastCheckedAt = now;
		refreshIfChanged();
		return cachedState;
	}

	private void refreshIfChanged()
	{
		Path saveFile = resolveSaveFile();
		if (saveFile == null || !Files.isRegularFile(saveFile))
		{
			cachedState = Optional.empty();
			lastModified = null;
			return;
		}

		try
		{
			FileTime modified = Files.getLastModifiedTime(saveFile);
			if (modified.equals(lastModified))
			{
				return;
			}

			lastModified = modified;
			cachedState = readAndParse(saveFile);
		}
		catch (IOException e)
		{
			log.debug("Failed to check osrs-tcg save file", e);
		}
	}

	private Optional<TcgState> readAndParse(Path saveFile)
	{
		try
		{
			String raw = Files.readString(saveFile, StandardCharsets.UTF_8);
			Optional<TcgState> parsed = TcgStateDecoder.decode(raw).flatMap(TcgStateParser::parse);
			parsed.ifPresent(state -> log.debug("Read osrs-tcg state: credits={} packs={} cards={} foils={}",
				state.getCredits(), state.getOpenedPacks(), state.getOwnedCards().size(), state.getFoilCount()));
			return parsed;
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
