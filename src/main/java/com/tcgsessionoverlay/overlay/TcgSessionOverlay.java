package com.tcgsessionoverlay.overlay;

import com.tcgsessionoverlay.TcgSessionOverlayConfig;
import com.tcgsessionoverlay.session.CreditsTracker;
import com.tcgsessionoverlay.session.RatesTracker;
import com.tcgsessionoverlay.session.SessionClock;
import com.tcgsessionoverlay.session.XpCountdownTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import javax.inject.Inject;
import net.runelite.api.Skill;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentConstants;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class TcgSessionOverlay extends OverlayPanel
{
	private static final Color SECTION_COLOR = Color.ORANGE;
	private static final int MINIMUM_PANEL_WIDTH = 160;

	private final TcgSessionOverlayConfig config;
	private final CreditsTracker creditsTracker;
	private final RatesTracker ratesTracker;
	private final SessionClock sessionClock;
	private final XpCountdownTracker xpCountdownTracker;

	private FontMetrics fontMetrics;
	private int requiredContentWidth;

	@Inject
	private TcgSessionOverlay(
		TcgSessionOverlayConfig config,
		CreditsTracker creditsTracker,
		RatesTracker ratesTracker,
		SessionClock sessionClock,
		XpCountdownTracker xpCountdownTracker)
	{
		this.config = config;
		this.creditsTracker = creditsTracker;
		this.ratesTracker = ratesTracker;
		this.sessionClock = sessionClock;
		this.xpCountdownTracker = xpCountdownTracker;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setGap(new Point(0, 4));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.hideWhenIdle() && sessionClock.isIdle())
		{
			return null;
		}

		fontMetrics = graphics.getFontMetrics();
		requiredContentWidth = 0;

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("TCG Session")
			.color(SECTION_COLOR)
			.build());

		renderCredits();
		renderRates();
		renderXpCountdown();

		panelComponent.setPreferredSize(new Dimension(panelWidth(), 0));
		setPreferredColor(config.backgroundColor());

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
			addWaiting("Waiting for TCG data");
			return;
		}

		long sessionCredits = creditsTracker.getSessionCreditsEarned();
		long packsAffordable = creditsTracker.getPacksAffordable();

		addLine("Balance", format(creditsTracker.getCredits()));
		addLine("This session", "+" + format(sessionCredits), highlightWhen(sessionCredits > 0));
		addLine("Lifetime", format(creditsTracker.getLifetimeCredits()));
		addLine("Ready to buy", format(packsAffordable), highlightWhen(packsAffordable > 0));
		addLine("Next pack", format(creditsTracker.getCreditsTowardNextPack())
			+ " / " + format(creditsTracker.getPackCost()));
	}

	private void renderRates()
	{
		if (!config.showRates())
		{
			return;
		}

		addSection("Rates");

		long creditsPerHour = ratesTracker.getCreditsPerHour();
		addLine("Credits / hr", creditsPerHour >= 0 ? format(creditsPerHour) : "-");
	}

	private void renderXpCountdown()
	{
		if (!config.showXpCountdown())
		{
			return;
		}

		addSection("XP Countdown");

		Skill trackedSkill = xpCountdownTracker.getTrackedSkill();
		if (trackedSkill == null)
		{
			addWaiting("Waiting for XP");
			return;
		}

		addSkillName(trackedSkill.getName());

		if (!xpCountdownTracker.isTrackedSkillEarningCredits())
		{
			addLine("Level up credits", format(xpCountdownTracker.getNextLevelCredits()));
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

	private void addWaiting(String message)
	{
		widen(message, "");
		panelComponent.getChildren().add(LineComponent.builder()
			.left(message)
			.leftColor(Color.LIGHT_GRAY)
			.build());
	}

	private void addSkillName(String name)
	{
		widen(name, "");
		panelComponent.getChildren().add(LineComponent.builder()
			.left(name)
			.build());
	}

	private int panelWidth()
	{
		return Math.max(MINIMUM_PANEL_WIDTH, requiredContentWidth + 2 * ComponentConstants.STANDARD_BORDER);
	}

	private void addSection(String name)
	{
		widen(name, "");
		panelComponent.getChildren().add(LineComponent.builder()
			.left(name)
			.leftColor(SECTION_COLOR)
			.build());
	}

	private Color highlightWhen(boolean condition)
	{
		return condition ? config.highlightColor() : Color.WHITE;
	}

	private void addLine(String label, String value)
	{
		addLine(label, value, Color.WHITE);
	}

	private void addLine(String label, String value, Color valueColor)
	{
		widen(label, value);
		panelComponent.getChildren().add(LineComponent.builder()
			.left(label)
			.right(value)
			.rightColor(valueColor)
			.build());
	}

	private void widen(String left, String right)
	{
		requiredContentWidth = Math.max(requiredContentWidth,
			fontMetrics.stringWidth(left) + fontMetrics.stringWidth(right));
	}

	private String format(long value)
	{
		return config.numberStyle().format(value);
	}
}
