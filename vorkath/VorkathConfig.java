package net.runelite.client.plugins.vorkath;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("vorkath")
public interface VorkathConfig extends Config {
	@ConfigItem(
		position = 0,
		keyName = "Vorkathenable",
		name = "Enable Vorkath Helper",
		description = "Configures whether or not to enable Vorkath Helper."
	)
	default boolean EnableVorkath() { return true; }

}
