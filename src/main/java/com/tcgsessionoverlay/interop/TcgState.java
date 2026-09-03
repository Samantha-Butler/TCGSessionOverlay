package com.tcgsessionoverlay.interop;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import net.runelite.api.Skill;

@Getter
public final class TcgState
{
	private final long credits;
	private final long totalCreditsGained;
	private final long profileSavedAtUnix;
	private final Map<Skill, Long> uncreditedXpBySkill;
	private final Map<Skill, Long> baselineSkillXp;

	public TcgState(
		long credits,
		long totalCreditsGained,
		long profileSavedAtUnix,
		Map<Skill, Long> uncreditedXpBySkill,
		Map<Skill, Long> baselineSkillXp)
	{
		this.credits = credits;
		this.totalCreditsGained = totalCreditsGained;
		this.profileSavedAtUnix = profileSavedAtUnix;
		this.uncreditedXpBySkill = copyOf(uncreditedXpBySkill);
		this.baselineSkillXp = copyOf(baselineSkillXp);
	}

	public long getUncreditedXp(Skill skill)
	{
		return uncreditedXpBySkill.getOrDefault(skill, 0L);
	}

	public boolean hasBaselineXp(Skill skill)
	{
		return baselineSkillXp.containsKey(skill);
	}

	public long getBaselineXp(Skill skill)
	{
		return baselineSkillXp.getOrDefault(skill, 0L);
	}

	private static Map<Skill, Long> copyOf(Map<Skill, Long> source)
	{
		Map<Skill, Long> copy = new EnumMap<>(Skill.class);
		copy.putAll(source);
		return Collections.unmodifiableMap(copy);
	}
}
