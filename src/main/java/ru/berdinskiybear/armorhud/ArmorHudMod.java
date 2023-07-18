package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.uku3lig.ukulib.config.ConfigManager;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

public final class ArmorHudMod {
    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, "ukus-armor-hud");

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return false;

        final int damage = stack.getDamage();
        final int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double) damage / maxDamage);

        return percentage <= manager.getConfig().getMinDurabilityPercentage()
                || maxDamage - damage <= manager.getConfig().getMinDurabilityValue();
    }

    private ArmorHudMod() {
    }
}
