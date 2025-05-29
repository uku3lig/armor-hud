package ru.berdinskiybear.armorhud;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.nio.file.Path;

public final class ArmorHudMod {
    public static final String MOD_ID = "ukus-armor-hud";
    
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyBinding TOGGLE_HUD = new KeyBinding("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN, "armorhud.name");
    
    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return false;

        final int damage = stack.getDamage();
        final int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double) damage / maxDamage);

        return percentage <= ArmorHudConfig.CONFIG.getMinDurabilityPercentage()
                || maxDamage - damage <= ArmorHudConfig.CONFIG.getMinDurabilityValue();
    }

    @ExpectPlatform
    public static Path configDir() {
        throw new AssertionError();
    }
}
