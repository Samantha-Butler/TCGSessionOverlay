package com.tcgsessionoverlay.interop;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TcgStateDecoderTest
{
	private static final String VERSION_PREFIX = "RLTCG_v2:";
	private static final byte[] SALT = "RLTCG|osrs-tcg!".getBytes(StandardCharsets.US_ASCII);

	@Test
	public void decodesKnownGoodFixture() throws Exception
	{
		String json = "{\"cardInstances\":[],\"credits\":12345}";
		String encoded = encode(json);

		Optional<String> decoded = TcgStateDecoder.decode(encoded);

		assertTrue(decoded.isPresent());
		assertEquals(json, decoded.get());
	}

	@Test
	public void rejectsMissingPrefix()
	{
		assertFalse(TcgStateDecoder.decode("not-a-valid-prefix:abc123").isPresent());
	}

	@Test
	public void rejectsNull()
	{
		assertFalse(TcgStateDecoder.decode(null).isPresent());
	}

	@Test
	public void rejectsCorruptPayload()
	{
		assertFalse(TcgStateDecoder.decode(VERSION_PREFIX + "not-valid-base64!!!").isPresent());
	}

	private static String encode(String json) throws Exception
	{
		ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(gzipped))
		{
			gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
		}

		byte[] gzippedBytes = gzipped.toByteArray();
		byte[] xored = new byte[gzippedBytes.length];
		for (int i = 0; i < gzippedBytes.length; i++)
		{
			xored[i] = (byte) (gzippedBytes[i] ^ SALT[i % SALT.length]);
		}

		return VERSION_PREFIX + Base64.getEncoder().encodeToString(xored);
	}
}
