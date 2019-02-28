package net.runelite.client.plugins.hydra;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hydra")
public interface HydraConfig extends Config {
	@ConfigItem(
		position = 0,
		keyName = "hydraenable",
		name = "Enable Hydra (194 cb) Helper",
		description = "Configures whether or not to enable Hydra Helper. (For use on regular hydra's only, will not work with Alchemical Hydra."
	)
	default boolean EnableHydra() { return true; }

	@ConfigItem(
			position = 1,
			keyName = "countersize",
			name = "Bold indicator",
			description = "Configures if text indicator is bold or not."
	)
	default boolean BoldText() { return true; }

}
