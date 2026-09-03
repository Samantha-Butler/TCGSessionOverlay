package com.tcgsessionoverlay.interop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import net.runelite.api.Skill;

@Getter
public final class TcgState
{
	private final long credits;
	private final long openedPacks;
	private final long totalCreditsGained;
	private final long profileSavedAtUnix;
	private final Map<Skill, Long> uncreditedXpBySkill;
	private final Map<Skill, Long> baselineSkillXp;
	private final List<OwnedCard> ownedCards;
	private final int uniqueCardNames;
	private final int foilCount;

	public TcgState(
		long credits,
		long openedPacks,
		long totalCreditsGained,
		long profileSavedAtUnix,
		Map<Skill, Long> uncreditedXpBySkill,
		Map<Skill, Long> baselineSkillXp,
		List<OwnedCard> ownedCards)
	{
		this.credits = credits;
		this.openedPacks = openedPacks;
		this.totalCreditsGained = totalCreditsGained;
		this.profileSavedAtUnix = profileSavedAtUnix;
		this.uncreditedXpBySkill = copyOf(uncreditedXpBySkill);
		this.baselineSkillXp = copyOf(baselineSkillXp);
		List<OwnedCard> collected = new ArrayList<>();
		for (OwnedCard card : ownedCards)
		{
			if (!card.isBeta())
			{
				collected.add(card);
			}
		}
		this.ownedCards = Collections.unmodifiableList(collected);

		Set<String> names = new HashSet<>();
		int foils = 0;
		for (OwnedCard card : this.ownedCards)
		{
			names.add(card.getCardName());
			if (card.isFoil())
			{
				foils++;
			}
		}
		this.uniqueCardNames = names.size();
		this.foilCount = foils;
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
