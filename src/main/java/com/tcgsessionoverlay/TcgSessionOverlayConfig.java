package com.tcgsessionoverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("tcgsessionoverlay")
public interface TcgSessionOverlayConfig extends Config
{
	@ConfigItem(
		keyName = "showCredits",
		name = "Show credits",
		description = "Show the credit balance, session earnings, packs you can buy now and progress to the next pack.",
		position = 1
	)
	default boolean showCredits()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showRates",
		name = "Show rates",
		description = "Show credits earned per hour. Time spent with no XP for over five minutes is not counted.",
		position = 2
	)
	default boolean showRates()
	{
		return true;
	}

	@Range(min = 1, max = 100000)
	@ConfigItem(
		keyName = "packCost",
		name = "Pack cost",
		description = "Credits per pack, taken from the Shop tab of the OSRS TCG panel. Press Enter or use the arrows to apply a typed value.",
		position = 3
	)
	default int packCost()
	{
		return 2500;
	}
}
