package com.tcgsessionoverlay.interop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;

// Reimplements osrs-tcg's own storage format, not imported, so there is no compile time link.
// The encoding is fixed and public, nothing secret is being reverse engineered.
@Slf4j
public final class TcgStateDecoder
{
	private static final String VERSION_PREFIX = "RLTCG_v2:";
	private static final byte[] SALT = "RLTCG|osrs-tcg!".getBytes(StandardCharsets.US_ASCII);

	private TcgStateDecoder()
	{
	}

	public static Optional<String> decode(String raw)
	{
		if (raw == null || !raw.startsWith(VERSION_PREFIX))
		{
			return Optional.empty();
		}

		try
		{
			byte[] decoded = Base64.getDecoder().decode(raw.substring(VERSION_PREFIX.length()));
			byte[] xored = xor(decoded);
			byte[] decompressed = gunzip(xored);
			return Optional.of(new String(decompressed, StandardCharsets.UTF_8));
		}
		catch (Exception e)
		{
			log.debug("Failed to decode osrs-tcg state", e);
			return Optional.empty();
		}
	}

	private static byte[] xor(byte[] data)
	{
		byte[] result = new byte[data.length];
		for (int i = 0; i < data.length; i++)
		{
			result[i] = (byte) (data[i] ^ SALT[i % SALT.length]);
		}
		return result;
	}

	private static byte[] gunzip(byte[] data) throws Exception
	{
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(data));
			ByteArrayOutputStream output = new ByteArrayOutputStream())
		{
			byte[] buffer = new byte[4096];
			int read;
			while ((read = gzipInputStream.read(buffer)) != -1)
			{
				output.write(buffer, 0, read);
			}
			return output.toByteArray();
		}
	}
}
