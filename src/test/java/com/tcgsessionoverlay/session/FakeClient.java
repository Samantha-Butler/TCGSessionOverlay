package com.tcgsessionoverlay.session;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumMap;
import java.util.Map;
import net.runelite.api.Client;
import net.runelite.api.Skill;

final class FakeClient
{
	private final Map<Skill, Integer> xpBySkill = new EnumMap<>(Skill.class);
	private final Client client = (Client) Proxy.newProxyInstance(
		Client.class.getClassLoader(),
		new Class<?>[]{Client.class},
		(proxy, method, args) -> answer(method, args));

	Client client()
	{
		return client;
	}

	void setXp(Skill skill, int xp)
	{
		xpBySkill.put(skill, xp);
	}

	private Object answer(Method method, Object[] args)
	{
		if (method.getName().equals("getSkillExperience"))
		{
			return xpBySkill.getOrDefault((Skill) args[0], 0);
		}

		throw new UnsupportedOperationException(method.getName());
	}
}
