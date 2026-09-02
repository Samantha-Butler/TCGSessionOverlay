package com.tcgsessionoverlay.interop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
	private final List<OwnedCard> ownedCards;
	private final int uniqueCardNames;
	private final int foilCount;

	public TcgState(
		long credits,
		long openedPacks,
		long totalCreditsGained,
		long profileSavedAtUnix,
		Map<Skill, Long> uncreditedXpBySkill,
		List<OwnedCard> ownedCards,
		int uniqueCardNames)
	{
		Map<Skill, Long> uncreditedCopy = new EnumMap<>(Skill.class);
		uncreditedCopy.putAll(uncreditedXpBySkill);

		this.credits = credits;
		this.openedPacks = openedPacks;
		this.totalCreditsGained = totalCreditsGained;
		this.profileSavedAtUnix = profileSavedAtUnix;
		this.uncreditedXpBySkill = Collections.unmodifiableMap(uncreditedCopy);
		this.ownedCards = Collections.unmodifiableList(new ArrayList<>(ownedCards));
		this.uniqueCardNames = uniqueCardNames;

		int foils = 0;
		for (OwnedCard card : this.ownedCards)
		{
			if (card.isFoil())
			{
				foils++;
			}
		}
		this.foilCount = foils;
	}

	public long getUncreditedXp(Skill skill)
	{
		return uncreditedXpBySkill.getOrDefault(skill, 0L);
	}
}
