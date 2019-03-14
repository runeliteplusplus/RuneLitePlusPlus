package net.runelite.client.plugins.freezetimers;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("freezetimers")
public interface FreezeTimersConfig extends Config {
	@ConfigItem(
		position = 0,
		keyName = "freezeenable",
		name = "Enable PvP freeze timers",
		description = "Configures whether or not to show freeze timers."
	)
	default boolean EnableFreezeTimers() { return false; }

	@ConfigItem(
			position = 1,
			keyName = "tilehighlight",
			name = "Frozen opponent tile highlighting",
			description = "Configures whether or not to highlight tiles frozen opponents are standing on."
	)
	default boolean drawTiles() { return false; }

	@ConfigItem(
		position = 2,
		keyName = "timercolor",
		name = "Freeze Timer Color",
		description = "Color of freeze timer"
	)
	default Color FreezeTimerColor() {
		return new Color(0, 184, 212);
	}

	@ConfigItem(
			position = 3,
			keyName = "timerpos",
			name = "Freeze Timer Position",
			description = "Position of freeze timer"
	)
	default int FreezeTimerPos() { return 80; }

	@ConfigItem(
			position = 4,
			keyName = "timerfont",
			name = "Bold Freeze Timer",
			description = "Bold font or not"
	)
	default boolean BoldFont() { return false; }

	@ConfigItem(
			position = 5,
			keyName = "tbtimer",
			name = "Tele Block Timer",
			description = "Enables tele block timer"
	)
	default boolean TBTimer() { return true; }
}
