package com.tcgsessionoverlay.session;

import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.api.Skill;

final class SavedStateReader extends TcgStateReader
{
	static final int HITPOINTS_XP = 1154;
	static final int FIREMAKING_XP = 13363;
	static final int FIREMAKING_CARRY = 300;

	private final TcgState state;

	private SavedStateReader(Client client, TcgState state)
	{
		super(client, null);
		this.state = state;
	}

	static SavedStateReader firemakingSave(Client client)
	{
		Map<Skill, Long> carry = new EnumMap<>(Skill.class);
		carry.put(Skill.FIREMAKING, (long) FIREMAKING_CARRY);

		Map<Skill, Long> baseline = new EnumMap<>(Skill.class);
		baseline.put(Skill.HITPOINTS, (long) HITPOINTS_XP);
		baseline.put(Skill.FIREMAKING, (long) FIREMAKING_XP);

		return new SavedStateReader(client, new TcgState(1L, carry, baseline));
	}

	static SavedStateReader emptySave(Client client)
	{
		return new SavedStateReader(client, new TcgState(1L, Collections.emptyMap(), Collections.emptyMap()));
	}

	@Override
	public Optional<TcgState> getState()
	{
		return Optional.of(state);
	}
}
