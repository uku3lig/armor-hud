package ru.berdinskiybear.armorhud.fabric;

import net.fabricmc.api.ClientModInitializer;
import ru.berdinskiybear.armorhud.ArmorHudMod;

public class ArmorHudFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmorHudMod.onInitialize();
    }
}
