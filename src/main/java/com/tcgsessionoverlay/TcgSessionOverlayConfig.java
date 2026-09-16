package com.tcgsessionoverlay;

import com.tcgsessionoverlay.overlay.NumberStyle;
import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
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
		description = "Show the credits earned this session.",
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

	@ConfigItem(
		keyName = "highlightColor",
		name = "Highlight",
		description = "Colour used once you have earned credits this session.",
		position = 81,
		section = appearanceSection
	)
	default Color highlightColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "hideWhenIdle",
		name = "Hide when idle",
		description = "Hide the overlay after five minutes with no XP. It returns on your next XP drop.",
		position = 82,
		section = appearanceSection
	)
	default boolean hideWhenIdle()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		keyName = "backgroundColor",
		name = "Background",
		description = "Overlay background colour. Drag the alpha slider left to make the panel more transparent.",
		position = 83,
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
		position = 84,
		section = appearanceSection
	)
	default NumberStyle numberStyle()
	{
		return NumberStyle.FULL;
	}
}
