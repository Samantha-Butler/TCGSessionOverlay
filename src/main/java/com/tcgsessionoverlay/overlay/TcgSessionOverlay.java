package com.tcgsessionoverlay.overlay;

import com.tcgsessionoverlay.TcgSessionOverlayConfig;
import com.tcgsessionoverlay.session.CreditsTracker;
import com.tcgsessionoverlay.session.RatesTracker;
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
import net.runelite.client.util.QuantityFormatter;

public class TcgSessionOverlay extends OverlayPanel
{
	private static final Color SECTION_COLOR = Color.ORANGE;
	private static final int PANEL_WIDTH = 160;

	private final TcgSessionOverlayConfig config;
	private final CreditsTracker creditsTracker;
	private final RatesTracker ratesTracker;
	private final XpCountdownTracker xpCountdownTracker;

	@Inject
	private TcgSessionOverlay(
		TcgSessionOverlayConfig config,
		CreditsTracker creditsTracker,
		RatesTracker ratesTracker,
		XpCountdownTracker xpCountdownTracker)
	{
		this.config = config;
		this.creditsTracker = creditsTracker;
		this.ratesTracker = ratesTracker;
		this.xpCountdownTracker = xpCountdownTracker;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setGap(new Point(0, 4));
		panelComponent.setPreferredSize(new Dimension(PANEL_WIDTH, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("TCG Session")
			.color(SECTION_COLOR)
			.build());

		renderCredits();
		renderRates();
		renderXpCountdown();

		return super.render(graphics);
	}

	private void renderCredits()
	{
		if (!config.showCredits())
		{
			return;
		}

		addSection("Credits");

		if (!creditsTracker.hasState())
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Waiting for TCG data")
				.leftColor(Color.LIGHT_GRAY)
				.build());
			return;
		}

		addLine("Balance", QuantityFormatter.formatNumber(creditsTracker.getCredits()));
		addLine("This session", "+" + QuantityFormatter.formatNumber(creditsTracker.getSessionCreditsEarned()));
		addLine("Ready to buy", QuantityFormatter.formatNumber(creditsTracker.getPacksAffordable()));
		addLine("Next pack", QuantityFormatter.formatNumber(creditsTracker.getCreditsTowardNextPack())
			+ " / " + QuantityFormatter.formatNumber(creditsTracker.getPackCost()));
	}

	private void renderRates()
	{
		if (!config.showRates())
		{
			return;
		}

		addSection("Rates");

		long creditsPerHour = ratesTracker.getCreditsPerHour();
		addLine("Credits / hr", creditsPerHour >= 0
			? QuantityFormatter.formatNumber(creditsPerHour)
			: "-");
	}

	private void renderXpCountdown()
	{
		addSection("XP Countdown");

		Skill trackedSkill = xpCountdownTracker.getTrackedSkill();
		if (trackedSkill == null)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left("Waiting for XP")
				.leftColor(Color.LIGHT_GRAY)
				.build());
			return;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(trackedSkill.getName())
			.build());

		if (!xpCountdownTracker.isTrackedSkillEarningCredits())
		{
			return;
		}

		ProgressBarComponent progressBar = new ProgressBarComponent();
		progressBar.setMaximum(xpCountdownTracker.getBlockSize());
		progressBar.setValue(xpCountdownTracker.getXpInCurrentBlock());
		progressBar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.FULL);
		panelComponent.getChildren().add(progressBar);

		int actionsRemaining = xpCountdownTracker.getActionsRemaining();
		addLine("Actions left", actionsRemaining >= 0 ? String.valueOf(actionsRemaining) : "-");

		int medianXpPerAction = xpCountdownTracker.getMedianXpPerAction();
		if (medianXpPerAction > 0)
		{
			addLine("Xp per action", String.valueOf(medianXpPerAction));
		}
	}

	private void addSection(String name)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(name)
			.leftColor(SECTION_COLOR)
			.build());
	}

	private void addLine(String label, String value)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(label)
			.right(value)
			.build());
	}
}
