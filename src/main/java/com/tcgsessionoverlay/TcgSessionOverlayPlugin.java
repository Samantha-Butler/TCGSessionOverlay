package com.tcgsessionoverlay;

import com.google.inject.Provides;
import com.tcgsessionoverlay.interop.TcgStateReader;
import com.tcgsessionoverlay.overlay.TcgSessionOverlay;
import com.tcgsessionoverlay.session.CreditsTracker;
import com.tcgsessionoverlay.session.XpCountdownTracker;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

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

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private TcgSessionOverlay overlay;

	@Inject
	private EventBus eventBus;

	@Inject
	private XpCountdownTracker xpCountdownTracker;

	@Inject
	private TcgStateReader tcgStateReader;

	@Inject
	private CreditsTracker creditsTracker;

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		eventBus.register(xpCountdownTracker);
		eventBus.register(tcgStateReader);
		eventBus.register(creditsTracker);
		log.debug("TCG Session Overlay started");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		eventBus.unregister(xpCountdownTracker);
		eventBus.unregister(tcgStateReader);
		eventBus.unregister(creditsTracker);
		log.debug("TCG Session Overlay stopped");
	}

	@Provides
	TcgSessionOverlayConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TcgSessionOverlayConfig.class);
	}
}
