package ru.berdinskiybear.armorhud.compat;

import me.juancarloscp52.bedrockify.client.BedrockifyClient;

public class BedrockifyCompat {
    public float hudOpacity() {
        return BedrockifyClient.getInstance().hudOpacity.getHudOpacity(false);
    }

    public int screenSafeArea() {
        return BedrockifyClient.getInstance().settings.getScreenSafeArea();
    }
}
