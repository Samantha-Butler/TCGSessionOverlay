package com.tcgsessionoverlay;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(
	name = "TCG Session Overlay"
)
public class TcgSessionOverlayPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TcgSessionOverlayConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.debug("TCG Session Overlay started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.debug("TCG Session Overlay stopped");
	}

	@Provides
	TcgSessionOverlayConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TcgSessionOverlayConfig.class);
	}
}
