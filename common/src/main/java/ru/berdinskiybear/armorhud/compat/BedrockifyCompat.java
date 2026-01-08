package ru.berdinskiybear.armorhud.compat;

// TODO
public class BedrockifyCompat {
    public float hudOpacity() {
        // return BedrockifyClient.getInstance().hudOpacity.getHudOpacity(false);
        return 1;
    }

    public int screenSafeArea() {
        // return BedrockifyClient.getInstance().settings.getScreenSafeArea();
        return 0;
    }
}
