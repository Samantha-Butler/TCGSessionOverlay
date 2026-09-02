package com.tcgsessionoverlay;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("tcgsessionoverlay")
public interface TcgSessionOverlayConfig extends Config
{
	@Range(min = 1, max = 100000)
	@ConfigItem(
		keyName = "packCost",
		name = "Pack cost",
		description = "Credits per pack, used for packs affordable and progress to the next pack. Set this to the price of the pack you buy, shown on the Shop tab of the OSRS TCG panel.",
		position = 1
	)
	default int packCost()
	{
		return 2500;
	}
}
