package com.tcgsessionoverlay.session;

import com.tcgsessionoverlay.interop.TcgState;
import com.tcgsessionoverlay.interop.TcgStateReader;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.runelite.api.Client;
import net.runelite.api.Skill;

final class SavedStateReader extends TcgStateReader
{
	static final long CREDITS = 586;
	static final int HITPOINTS_XP = 1154;
	static final int FIREMAKING_XP = 13363;
	static final int FIREMAKING_CARRY = 300;

	private final TcgState state;

	SavedStateReader(Client client)
	{
		super(client, null);

		Map<Skill, Long> carry = new EnumMap<>(Skill.class);
		carry.put(Skill.FIREMAKING, (long) FIREMAKING_CARRY);

		Map<Skill, Long> baseline = new EnumMap<>(Skill.class);
		baseline.put(Skill.HITPOINTS, (long) HITPOINTS_XP);
		baseline.put(Skill.FIREMAKING, (long) FIREMAKING_XP);

		state = new TcgState(CREDITS, CREDITS, 1L, carry, baseline);
	}

	@Override
	public Optional<TcgState> getState()
	{
		return Optional.of(state);
	}
}
