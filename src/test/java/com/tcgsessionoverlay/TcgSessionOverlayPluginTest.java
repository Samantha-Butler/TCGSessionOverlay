package com.tcgsessionoverlay;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TcgSessionOverlayPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TcgSessionOverlayPlugin.class);
		RuneLite.main(args);
	}
}
