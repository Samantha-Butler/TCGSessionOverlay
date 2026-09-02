package com.tcgsessionoverlay.overlay;

import com.tcgsessionoverlay.session.XpCountdownTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.inject.Inject;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TcgSessionOverlay extends OverlayPanel
{
	private final XpCountdownTracker xpCountdownTracker;

	@Inject
	private TcgSessionOverlay(XpCountdownTracker xpCountdownTracker)
	{
		this.xpCountdownTracker = xpCountdownTracker;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setGap(new Point(0, 4));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("TCG Session")
			.color(Color.ORANGE)
			.build());

		renderXpCountdown();

		return super.render(graphics);
	}

	private void renderXpCountdown()
	{
		Skill trackedSkill = xpCountdownTracker.getTrackedSkill();
		if (trackedSkill == null)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("Waiting for XP")
				.color(Color.LIGHT_GRAY)
				.build());
			return;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(trackedSkill.getName())
			.build());

		ProgressBarComponent progressBar = new ProgressBarComponent();
		progressBar.setMaximum(1000);
		progressBar.setValue(xpCountdownTracker.getXpInCurrentBlock());
		progressBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.FULL);
		panelComponent.getChildren().add(progressBar);

		int actionsRemaining = xpCountdownTracker.getActionsRemaining();
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Actions left")
			.right(actionsRemaining >= 0 ? String.valueOf(actionsRemaining) : "-")
			.build());

		int medianXpPerAction = xpCountdownTracker.getMedianXpPerAction();
		if (medianXpPerAction > 0)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Xp per action")
				.right(String.valueOf(medianXpPerAction))
				.build());
		}
	}
}
