package net.runelite.client.plugins.freezetimers;

import net.runelite.client.eventbus.Subscribe;
import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

import net.runelite.api.*;

import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GraphicChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Freeze Timers",
	description = "PVP Freeze Timers",
	tags = {"PvP", "Freeze", "Timers"}
)
public class FreezeTimersPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private FreezeTimersConfig config;

	@Inject
	private FreezeTimersOverlay FreezeTimersOverlay;

	@Inject
	private FreezeTimersTileOverlay FreezeTimersTileOverlay;

	@Inject
	private Client client;

	@Inject
	private SpriteManager spriteManager;

	@Provides
	FreezeTimersConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(FreezeTimersConfig.class);
	}

	private static final int[] FREEZE_ICONS = {
		SpriteID.SPELL_ENTANGLE,
		SpriteID.SPELL_SNARE,
		SpriteID.SPELL_BIND,
		SpriteID.SPELL_ICE_RUSH,
		SpriteID.SPELL_ICE_BURST,
		SpriteID.SPELL_ICE_BLITZ,
		SpriteID.SPELL_ICE_BARRAGE
	};

	private static final Dimension FREEZE_ICON_DIMENSION = new Dimension(25, 25);
	private static final Color FREEZE_ICON_OUTLINE_COLOR = new Color(33, 33, 33);
	private final BufferedImage[] FreezeIcons = new BufferedImage[FREEZE_ICONS.length];

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			loadFreezeIcons();
		}
	}

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(FreezeTimersOverlay);
		overlayManager.add(FreezeTimersTileOverlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(FreezeTimersOverlay);
		overlayManager.remove(FreezeTimersTileOverlay);
		frozenthings.clear();
		frozenthingpoints.clear();
	}

	Map<String, Long> frozenthings = new HashMap<>();
	Map<String, WorldPoint> frozenthingpoints = new HashMap<>();
	Map<String, Integer> freezetype = new HashMap<>();

	@Subscribe
	public void onGraphicChanged(GraphicChanged event)
	{
		if (!config.EnableFreezeTimers()) {
			return;
		}

		Actor actor = event.getActor();

		if (actor instanceof NPC) {
			return;
		}

		if (frozenthings.containsKey(actor.getName())) {
			return;
		}

		if (actor.getGraphic() == GraphicID.ENTANGLE) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 1);
		} else if (actor.getGraphic() == GraphicID.SNARE) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 2);
		} else if (actor.getGraphic() == GraphicID.BIND) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 3);
		} else if (actor.getGraphic() == GraphicID.ICE_RUSH) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 4);
		} else if (actor.getGraphic() == GraphicID.ICE_BURST) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 5);
		} else if (actor.getGraphic() == GraphicID.ICE_BLITZ) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 6);
		} else if (actor.getGraphic() == GraphicID.ICE_BARRAGE) {
			frozenthings.put(actor.getName(), System.currentTimeMillis());
			frozenthingpoints.put(actor.getName(), actor.getWorldLocation());
			freezetype.put(actor.getName(), 7);
		}
	}

	public long opponentfreezetime(String name) {
		if (frozenthings.containsKey(name)) {
			return frozenthings.get(name);
		}
		return 0;
	}

	public WorldPoint playerpos(String name) {
		if (frozenthingpoints.containsKey(name)) {
			return frozenthingpoints.get(name);
		}
		return null;
	}

	public void updatepos(String name, WorldPoint point) {
		if (frozenthingpoints.containsKey(name)) {
			frozenthingpoints.remove(name);
			frozenthingpoints.put(name, point);
		}
	}

	public int freezetype(String name) {
		if (freezetype.containsKey(name)) {
			return freezetype.get(name);
		}
		return 0;
	}

	public void deleteopponent(String name) {
		if (frozenthings.containsKey(name)) {
			frozenthings.remove(name);
		}
		if (frozenthingpoints.containsKey(name)) {
			frozenthingpoints.remove(name);
		}
		if (freezetype.containsKey(name)) {
			freezetype.remove(name);
		}
	}

	private void loadFreezeIcons() {
		final IndexedSprite[] freezeIcons = {};
		final IndexedSprite[] newfreezeIcons = Arrays.copyOf(freezeIcons, FREEZE_ICONS.length);
		int curPosition = 0;

		for (int i = 0; i < FREEZE_ICONS.length; i++, curPosition++)
		{
			final int resource = FREEZE_ICONS[i];
			FreezeIcons[i] = rgbaToIndexedBufferedImage(FreezeIconFromSprite(spriteManager.getSprite(resource, 0)));
			newfreezeIcons[curPosition] = createIndexedSprite(client, FreezeIcons[i]);
		}
	}

	private static IndexedSprite createIndexedSprite(final Client client, final BufferedImage bufferedImage) {
		final IndexColorModel indexedCM = (IndexColorModel) bufferedImage.getColorModel();

		final int width = bufferedImage.getWidth();
		final int height = bufferedImage.getHeight();
		final byte[] pixels = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
		final int[] palette = new int[indexedCM.getMapSize()];
		indexedCM.getRGBs(palette);

		final IndexedSprite newIndexedSprite = client.createIndexedSprite();
		newIndexedSprite.setPixels(pixels);
		newIndexedSprite.setPalette(palette);
		newIndexedSprite.setWidth(width);
		newIndexedSprite.setHeight(height);
		newIndexedSprite.setOriginalWidth(width);
		newIndexedSprite.setOriginalHeight(height);
		newIndexedSprite.setOffsetX(0);
		newIndexedSprite.setOffsetY(0);
		return newIndexedSprite;
	}

	private static BufferedImage rgbaToIndexedBufferedImage(final BufferedImage sourceBufferedImage) {
		final BufferedImage indexedImage = new BufferedImage(
				sourceBufferedImage.getWidth(),
				sourceBufferedImage.getHeight(),
				BufferedImage.TYPE_BYTE_INDEXED);

		final ColorModel cm = indexedImage.getColorModel();
		final IndexColorModel icm = (IndexColorModel) cm;

		final int size = icm.getMapSize();
		final byte[] reds = new byte[size];
		final byte[] greens = new byte[size];
		final byte[] blues = new byte[size];
		icm.getReds(reds);
		icm.getGreens(greens);
		icm.getBlues(blues);

		final WritableRaster raster = indexedImage.getRaster();
		final int pixel = raster.getSample(0, 0, 0);
		final IndexColorModel resultIcm = new IndexColorModel(8, size, reds, greens, blues, pixel);
		final BufferedImage resultIndexedImage = new BufferedImage(resultIcm, raster, sourceBufferedImage.isAlphaPremultiplied(), null);
		resultIndexedImage.getGraphics().drawImage(sourceBufferedImage, 0, 0, null);
		return resultIndexedImage;
	}

	private static BufferedImage FreezeIconFromSprite(final BufferedImage freezeSprite) {
		final BufferedImage freezeCanvas = ImageUtil.resizeCanvas(freezeSprite, FREEZE_ICON_DIMENSION.width, FREEZE_ICON_DIMENSION.height);
		return ImageUtil.outlineImage(freezeCanvas, FREEZE_ICON_OUTLINE_COLOR);
	}

	BufferedImage GetFreezeIcon(int id) {
		return FreezeIcons[id];
	}
}
