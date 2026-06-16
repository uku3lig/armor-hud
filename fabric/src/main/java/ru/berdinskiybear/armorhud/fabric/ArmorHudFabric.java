package ru.berdinskiybear.armorhud.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.berdinskiybear.armorhud.ArmorHudMod;

public class ArmorHudFabric implements ClientModInitializer {
    private static final Logger log = LoggerFactory.getLogger(ArmorHudFabric.class);

    static {
        try {
            Class.forName("me.juancarloscp52.bedrockify.client.BedrockifyClient");
            ArmorHudMod.setModCompat(new BedrockifyCompat());
        } catch (Exception e) {
            log.debug("Not enabling Bedrockify compatibility");
        }
    }

    @Override
    public void onInitializeClient() {
        ArmorHudMod.onInitialize();
    }
}
