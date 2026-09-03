package com.tcgsessionoverlay.interop;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.EnumMap;
import java.util.HashMap;
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

			return Optional.of(new TcgState(
				readLong(root, "credits"),
				readLong(root, "totalCreditsGained"),
				readLong(root, "profileSavedAtUnix"),
				parseSkillMap(root, "uncreditedXpBySkill"),
				parseSkillMap(root, "skillXp")));
		}
		catch (Exception e)
		{
			log.debug("Failed to parse osrs-tcg state", e);
			return Optional.empty();
		}
	}

	private static Map<Skill, Long> parseSkillMap(JsonObject root, String key)
	{
		Map<Skill, Long> bySkill = new EnumMap<>(Skill.class);
		JsonElement baselineElement = root.get("skillCreditBaseline");
		if (baselineElement == null || !baselineElement.isJsonObject())
		{
			return bySkill;
		}

		JsonElement mapElement = baselineElement.getAsJsonObject().get(key);
		if (mapElement == null || !mapElement.isJsonObject())
		{
			return bySkill;
		}

		for (Map.Entry<String, JsonElement> entry : mapElement.getAsJsonObject().entrySet())
		{
			Skill skill = SKILLS_BY_NAME.get(entry.getKey());
			if (skill == null || !entry.getValue().isJsonPrimitive())
			{
				continue;
			}

			bySkill.put(skill, entry.getValue().getAsLong());
		}

		return bySkill;
	}

	private static long readLong(JsonObject object, String key)
	{
		JsonElement element = object.get(key);
		return element != null && element.isJsonPrimitive() ? element.getAsLong() : 0L;
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
