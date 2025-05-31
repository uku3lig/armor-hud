package ru.berdinskiybear.armorhud.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

public class ArmorHudModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ArmorHudConfig.CONFIG::createScreen;
    }
}
