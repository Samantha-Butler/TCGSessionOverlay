package com.tcgsessionoverlay.overlay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TcgSessionOverlay extends OverlayPanel
{
	@Inject
	private TcgSessionOverlay()
	{
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("TCG Session")
			.color(Color.ORANGE)
			.build());

		return panelComponent.render(graphics);
	}
}
