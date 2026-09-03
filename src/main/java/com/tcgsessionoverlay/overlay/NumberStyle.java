package com.tcgsessionoverlay.overlay;

import net.runelite.client.util.QuantityFormatter;

public enum NumberStyle
{
	FULL("Full"),
	SHORT("Short");

	private final String label;

	NumberStyle(String label)
	{
		this.label = label;
	}

	public String format(long value)
	{
		return this == SHORT ? QuantityFormatter.quantityToStackSize(value) : QuantityFormatter.formatNumber(value);
	}

	@Override
	public String toString()
	{
		return label;
	}
}
