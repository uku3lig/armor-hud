package ru.berdinskiybear.armorhud.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.nio.file.Path;

public class ArmorHudModImpl implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ArmorHudMod.TOGGLE_HUD);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ArmorHudMod.TOGGLE_HUD.wasPressed())
                ArmorHudConfig.CONFIG.toggleEnabled();
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ArmorHudConfig.CONFIG.save());
    }

    public static Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
