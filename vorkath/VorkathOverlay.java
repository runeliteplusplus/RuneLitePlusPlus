package net.runelite.client.plugins.vorkath;

import java.awt.*;
import javax.inject.Inject;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.PanelComponent;

public class VorkathOverlay extends Overlay {
	private final VorkathConfig config;
	private final VorkathPlugin plugin;
	private final PanelComponent panelComponent = new PanelComponent();


	@Inject
	private Client client;

	@Inject
	private VorkathOverlay(VorkathConfig config, VorkathPlugin plugin) {
		this.config = config;
		this.plugin = plugin;
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setPriority(OverlayPriority.MED);
		panelComponent.setPreferredSize(new Dimension(150, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics) {
		NPC Vorkath = plugin.Vorkath;
		if (Vorkath != null) {
			if (plugin.fireball != null) {
				final Polygon poly = Perspective.getCanvasTilePoly(client, plugin.fireball);
				if (poly != null) {
					OverlayUtil.renderPolygon(graphics, poly, Color.RED);
				}
			}

			if (plugin.venomticks != 0) {
				if (plugin.venomticks + 5 <= plugin.ticks) {
					OverlayUtil.renderTextLocation(graphics, Vorkath.getCanvasTextLocation(graphics, Integer.toString(30 - (plugin.ticks - plugin.venomticks)), Vorkath.getLogicalHeight() + 150), Integer.toString(30 - (plugin.ticks - plugin.venomticks)), Color.ORANGE);
				}
			}

			OverlayUtil.renderTextLocation(graphics, Vorkath.getCanvasTextLocation(graphics, Integer.toString(7 - plugin.hits), Vorkath.getLogicalHeight() + 40), Integer.toString(7 - plugin.hits), Color.WHITE);
		}
		return null;
	}
}
