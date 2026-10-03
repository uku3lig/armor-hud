package ru.berdinskiybear.armorhud.neoforge;

import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.uku3lig.ukulib.neoforge.UkulibNFProvider;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.ArmorHudRenderer;
import ru.berdinskiybear.armorhud.config.UkulibIntegration;

@Mod(value = "ukus_armor_hud", dist = Dist.CLIENT)
public class ArmorHudNeoForge {
    public ArmorHudNeoForge(ModContainer container, IEventBus modBus) {
        ArmorHudMod.onInitialize();
        container.registerExtensionPoint(UkulibNFProvider.class, UkulibIntegration::new);
        modBus.addListener(this::registerArmorHudRenderer);
    }

    private void registerArmorHudRenderer(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Identifier.fromNamespaceAndPath(ArmorHudMod.MOD_ID, "gui_armor_hud"), (g, dt) -> {
            ArmorHudRenderer.drawArmorHud(g, dt);
            ArmorHudRenderer.renderMainAndOffhand(g);
        });
    }
}
