package com.tcgsessionoverlay.interop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Skill;

@Slf4j
public final class TcgStateParser
{
	private static final Gson GSON = new Gson();
	private static final Map<String, Skill> SKILLS_BY_NAME = skillsByName();

	private TcgStateParser()
	{
	}

	public static Optional<TcgState> parse(String json)
	{
		if (json == null || json.isEmpty())
		{
			return Optional.empty();
		}

		try
		{
			JsonObject root = GSON.fromJson(json, JsonObject.class);
			if (root == null)
			{
				return Optional.empty();
			}

			List<OwnedCard> ownedCards = parseOwnedCards(root);

			return Optional.of(new TcgState(
				readLong(root, "credits"),
				readLong(root, "openedPacks"),
				readLong(root, "totalCreditsGained"),
				readLong(root, "profileSavedAtUnix"),
				parseUncreditedXp(root),
				ownedCards,
				countCardEntries(root)));
		}
		catch (Exception e)
		{
			log.debug("Failed to parse osrs-tcg state", e);
			return Optional.empty();
		}
	}

	private static List<OwnedCard> parseOwnedCards(JsonObject root)
	{
		List<OwnedCard> cards = new ArrayList<>();
		JsonArray entries = readArray(root, "cardEntries");
		if (entries == null)
		{
			return cards;
		}

		for (JsonElement entryElement : entries)
		{
			if (!entryElement.isJsonObject())
			{
				continue;
			}

			JsonObject entry = entryElement.getAsJsonObject();
			String cardName = readString(entry, "cardName");
			JsonArray variants = readArray(entry, "variants");
			if (cardName == null || variants == null)
			{
				continue;
			}

			for (JsonElement variantElement : variants)
			{
				if (!variantElement.isJsonObject())
				{
					continue;
				}

				JsonObject variant = variantElement.getAsJsonObject();
				cards.add(new OwnedCard(
					cardName,
					readLong(variant, "pulledAt"),
					readBoolean(variant, "foil"),
					readBoolean(variant, "beta")));
			}
		}

		return cards;
	}

	private static Map<Skill, Long> parseUncreditedXp(JsonObject root)
	{
		Map<Skill, Long> uncredited = new EnumMap<>(Skill.class);
		JsonElement baselineElement = root.get("skillCreditBaseline");
		if (baselineElement == null || !baselineElement.isJsonObject())
		{
			return uncredited;
		}

		JsonElement uncreditedElement = baselineElement.getAsJsonObject().get("uncreditedXpBySkill");
		if (uncreditedElement == null || !uncreditedElement.isJsonObject())
		{
			return uncredited;
		}

		for (Map.Entry<String, JsonElement> entry : uncreditedElement.getAsJsonObject().entrySet())
		{
			Skill skill = SKILLS_BY_NAME.get(entry.getKey());
			if (skill == null || !entry.getValue().isJsonPrimitive())
			{
				continue;
			}

			uncredited.put(skill, entry.getValue().getAsLong());
		}

		return uncredited;
	}

	private static int countCardEntries(JsonObject root)
	{
		JsonArray entries = readArray(root, "cardEntries");
		return entries == null ? 0 : entries.size();
	}

	private static JsonArray readArray(JsonObject object, String key)
	{
		JsonElement element = object.get(key);
		return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
	}

	private static String readString(JsonObject object, String key)
	{
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
	}

	private static long readLong(JsonObject object, String key)
	{
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsLong() : 0L;
	}

	private static boolean readBoolean(JsonObject object, String key)
	{
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() && element.getAsBoolean();
	}

	private static Map<String, Skill> skillsByName()
	{
		Map<String, Skill> byName = new HashMap<>();
		for (Skill skill : Skill.values())
		{
			byName.put(skill.getName(), skill);
		}
		return byName;
	}
}
