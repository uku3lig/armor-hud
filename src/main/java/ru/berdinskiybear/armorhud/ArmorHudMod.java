package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.minecraft.text.Text;
import net.uku3lig.ukulib.config.ConfigManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

public final class ArmorHudMod {
    public static final String MOD_ID = "armor_hud";
    public static final String MOD_NAME = "BerdinskiyBear's ArmorHUD";

    public static final String FABRIC_RESOURCE_LOADER_ID = "fabric-resource-loader-v0";
    public static final String CLOTH_CONFIG_ID = "cloth-config2";

    public static final Text CONFIG_SCREEN_NAME = Text.translatable("armorHud.configScreen.title");

    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, "ukus-armor-hud");

    public static void log(String message) {
        log(Level.INFO, message);
    }

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }
}
