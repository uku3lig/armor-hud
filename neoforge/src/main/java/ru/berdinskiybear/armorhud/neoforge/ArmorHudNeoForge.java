package ru.berdinskiybear.armorhud.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.uku3lig.ukulib.neoforge.UkulibNFProvider;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.UkulibIntegration;

@Mod(value = "ukus_armor_hud", dist = Dist.CLIENT)
public class ArmorHudNeoForge {
    public ArmorHudNeoForge(ModContainer container) {
        ArmorHudMod.onInitialize();
        container.registerExtensionPoint(UkulibNFProvider.class, UkulibIntegration::new);
    }
}
