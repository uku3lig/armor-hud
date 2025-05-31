package ru.berdinskiybear.armorhud.neoforge;

import me.shedaniel.clothconfig.ClothConfigForgeDemo;
import me.shedaniel.clothconfig2.ClothConfigDemo;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.nio.file.Path;

@Mod(ArmorHudMod.MOD_ID)
@EventBusSubscriber
public class ArmorHudModImpl {
    public ArmorHudModImpl(IEventBus bus, Dist dist) {
        bus.addListener(ArmorHudModImpl::registerKeybinds);
        if (dist.isClient())
            ModLoadingContext.get().registerExtensionPoint(
                    IConfigScreenFactory.class,
                    () -> (container, parent) -> ArmorHudConfig.CONFIG.createScreen(parent));
    }

    public static Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @SubscribeEvent
    public static void endClientTick(ClientTickEvent.Post event) {
        while (ArmorHudMod.TOGGLE_HUD.wasPressed())
            ArmorHudConfig.CONFIG.toggleEnabled();
    }

    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(ArmorHudMod.TOGGLE_HUD);
    }
}
