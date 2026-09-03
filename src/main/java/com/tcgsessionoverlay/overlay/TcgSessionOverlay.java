package com.tcgsessionoverlay.overlay;

import com.tcgsessionoverlay.TcgSessionOverlayConfig;
import com.tcgsessionoverlay.session.CreditsTracker;
import com.tcgsessionoverlay.session.RatesTracker;
import com.tcgsessionoverlay.session.XpCountdownTracker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
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
	private final XpCountdownTracker xpCountdownTracker;

	private FontMetrics fontMetrics;
	private int requiredContentWidth;

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
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		fontMetrics = graphics.getFontMetrics();
		requiredContentWidth = 0;

		if (config.compactMode())
		{
			renderCompact();
		}
		else
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text("TCG Session")
				.color(SECTION_COLOR)
				.build());

			renderCredits();
			renderRates();
			renderXpCountdown();
		}

		panelComponent.setPreferredSize(new Dimension(panelWidth(), 0));
		setPreferredColor(config.backgroundColor());

		return super.render(graphics);
	}

	private void renderCompact()
	{
		List<String> segments = new ArrayList<>();

		if (config.showCredits() && creditsTracker.hasState())
		{
			segments.add(format(creditsTracker.getCredits()) + "cr");
			segments.add("+" + format(creditsTracker.getSessionCreditsEarned()));
		}

		String xpSegment = compactXpSegment();
		if (xpSegment != null)
		{
			segments.add(xpSegment);
		}

		if (config.showRates())
		{
			long creditsPerHour = ratesTracker.getCreditsPerHour();
			segments.add((creditsPerHour >= 0 ? format(creditsPerHour) : "-") + "cr/hr");
		}

		if (segments.isEmpty())
		{
			addWaiting("Waiting for TCG data");
			return;
		}

		String line = String.join("  ", segments);
		widen(line, "");
		panelComponent.getChildren().add(LineComponent.builder()
			.left(line)
			.build());
	}

	private String compactXpSegment()
	{
		if (!config.showXpCountdown() || xpCountdownTracker.getTrackedSkill() == null)
		{
			return null;
		}

		if (!xpCountdownTracker.isTrackedSkillEarningCredits())
		{
			return "Lvl " + format(xpCountdownTracker.getNextLevelCredits());
		}

		return "XP " + xpCountdownTracker.getXpInCurrentBlock() + "/" + xpCountdownTracker.getBlockSize();
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

		addLine("Balance", format(creditsTracker.getCredits()));
		addLine("This session", "+" + format(creditsTracker.getSessionCreditsEarned()));
		addLine("Lifetime", format(creditsTracker.getLifetimeCredits()));
		addLine("Ready to buy", format(creditsTracker.getPacksAffordable()));
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

	private void addLine(String label, String value)
	{
		widen(label, value);
		panelComponent.getChildren().add(LineComponent.builder()
			.left(label)
			.right(value)
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
