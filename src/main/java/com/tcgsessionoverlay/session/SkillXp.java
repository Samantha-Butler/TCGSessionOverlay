package com.tcgsessionoverlay.session;

import net.runelite.api.Client;
import net.runelite.api.Skill;

final class SkillXp
{
	private SkillXp()
	{
	}

	static boolean isLoaded(Client client)
	{
		return client.getSkillExperience(Skill.HITPOINTS) > 0;
	}
}
