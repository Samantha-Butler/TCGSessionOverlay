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
	private static final String VERSION_PREFIX_V3 = "RLTCG_v3:";
	private static final String VERSION_PREFIX_V2 = "RLTCG_v2:";
	private static final byte[] SALT = "RLTCG|osrs-tcg!".getBytes(StandardCharsets.US_ASCII);

	@Test
	public void decodesV3Fixture() throws Exception
	{
		String json = "{\"cardInstances\":[],\"credits\":12345}";
		String encoded = encodeV3(json);

		Optional<String> decoded = TcgStateDecoder.decode(encoded);

		assertTrue(decoded.isPresent());
		assertEquals(json, decoded.get());
	}

	@Test
	public void decodesLegacyV2Fixture() throws Exception
	{
		String json = "{\"cardInstances\":[],\"credits\":12345}";
		String encoded = encodeV2(json);

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
		assertFalse(TcgStateDecoder.decode(VERSION_PREFIX_V3 + "not-valid-base64!!!").isPresent());
	}

	private static String encodeV3(String json) throws Exception
	{
		return VERSION_PREFIX_V3 + Base64.getEncoder().encodeToString(gzip(json));
	}

	private static String encodeV2(String json) throws Exception
	{
		byte[] gzipped = gzip(json);
		byte[] xored = new byte[gzipped.length];
		for (int i = 0; i < gzipped.length; i++)
		{
			xored[i] = (byte) (gzipped[i] ^ SALT[i % SALT.length]);
		}

		return VERSION_PREFIX_V2 + Base64.getEncoder().encodeToString(xored);
	}

	private static byte[] gzip(String json) throws Exception
	{
		ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(gzipped))
		{
			gzipOutputStream.write(json.getBytes(StandardCharsets.UTF_8));
		}

		return gzipped.toByteArray();
	}
}
