package ru.berdinskiybear.armorhud.fabric;

import me.juancarloscp52.bedrockify.client.BedrockifyClient;
import ru.berdinskiybear.armorhud.compat.ModCompat;

public class BedrockifyCompat implements ModCompat {
    @Override
    public float hudOpacity() {
        return BedrockifyClient.getInstance().hudOpacity.getHudOpacity(false);
    }

    @Override
    public int screenSafeArea() {
        return BedrockifyClient.getInstance().settings.getScreenSafeArea();
    }
}
