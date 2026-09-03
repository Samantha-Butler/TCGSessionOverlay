package com.tcgsessionoverlay;

import com.tcgsessionoverlay.overlay.NumberStyle;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;
import net.runelite.client.ui.overlay.components.ComponentConstants;

@ConfigGroup("tcgsessionoverlay")
public interface TcgSessionOverlayConfig extends Config
{
	@ConfigSection(
		name = "Appearance",
		description = "How the overlay looks.",
		position = 80
	)
	String appearanceSection = "appearance";

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

	@ConfigItem(
		keyName = "showXpCountdown",
		name = "Show XP countdown",
		description = "Show progress to the next credit award for the skill you are training.",
		position = 3
	)
	default boolean showXpCountdown()
	{
		return true;
	}

	@Range(min = 1, max = 100000)
	@ConfigItem(
		keyName = "packCost",
		name = "Pack cost",
		description = "Credits per pack, taken from the Shop tab of the OSRS TCG panel. Press Enter or use the arrows to apply a typed value.",
		position = 4
	)
	default int packCost()
	{
		return 2500;
	}

	@ConfigItem(
		keyName = "compactMode",
		name = "Compact mode",
		description = "Show everything on one line instead of separate sections.",
		position = 81,
		section = appearanceSection
	)
	default boolean compactMode()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "backgroundColor",
		name = "Background",
		description = "Overlay background colour. Drag the alpha slider left to make the panel more transparent.",
		position = 82,
		section = appearanceSection
	)
	default Color backgroundColor()
	{
		return ComponentConstants.STANDARD_BACKGROUND_COLOR;
	}

	@ConfigItem(
		keyName = "numberStyle",
		name = "Number format",
		description = "Full shows 199,982. Short shows 199.9K.",
		position = 83,
		section = appearanceSection
	)
	default NumberStyle numberStyle()
	{
		return NumberStyle.FULL;
	}
}
