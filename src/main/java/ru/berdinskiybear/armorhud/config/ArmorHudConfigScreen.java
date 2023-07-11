package ru.berdinskiybear.armorhud.config;

import net.minecraft.client.gui.screen.Screen;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.config.option.CyclingOption;
import net.uku3lig.ukulib.config.option.SliderOption;
import net.uku3lig.ukulib.config.option.TypedInputOption;
import net.uku3lig.ukulib.config.option.WidgetCreator;
import net.uku3lig.ukulib.config.screen.AbstractConfigScreen;
import ru.berdinskiybear.armorhud.ArmorHudMod;

import java.util.Optional;

public class ArmorHudConfigScreen extends AbstractConfigScreen<ArmorHudConfig> {
    protected ArmorHudConfigScreen(Screen parent) {
        super("armorhud.config", parent, ArmorHudMod.getManager());
    }

    @Override
    protected WidgetCreator[] getWidgets(ArmorHudConfig config) {
        return new WidgetCreator[] {
                CyclingOption.ofBoolean("armorhud.option.enabled", config.isEnabled(), config::setEnabled),
                CyclingOption.ofEnum("armorhud.option.anchor", ArmorHudConfig.Anchor.class, config.getAnchor(), config::setAnchor),
                CyclingOption.ofEnum("armorhud.option.side", ArmorHudConfig.Side.class, config.getSide(), config::setSide),
                new TypedInputOption<>("armorhud.option.offsetX", String.valueOf(config.getOffsetX()), config::setOffsetX, this::getInt),
                new TypedInputOption<>("armorhud.option.offsetY", String.valueOf(config.getOffsetY()), config::setOffsetY, this::getInt),
                CyclingOption.ofEnum("armorhud.option.style", ArmorHudConfig.Style.class, config.getStyle(), config::setStyle),
                CyclingOption.ofEnum("armorhud.option.widgetShown", ArmorHudConfig.WidgetShown.class, config.getWidgetShown(), config::setWidgetShown),
                CyclingOption.ofEnum("armorhud.option.offhandSlotBehavior", ArmorHudConfig.OffhandSlotBehavior.class, config.getOffhandSlotBehavior(), config::setOffhandSlotBehavior),
                CyclingOption.ofBoolean("armorhud.option.pushBossbars", config.isPushBossbars(), config::setPushBossbars),
                CyclingOption.ofBoolean("armorhud.option.pushIcons", config.isPushStatusEffectIcons(), config::setPushStatusEffectIcons),
                CyclingOption.ofBoolean("armorhud.option.pushSubtitles", config.isPushSubtitles(), config::setPushSubtitles),
                CyclingOption.ofBoolean("armorhud.option.reversed", config.isReversed(), config::setReversed),
                CyclingOption.ofBoolean("armorhud.option.showIcons", config.isIconsShown(), config::setIconsShown),
                CyclingOption.ofBoolean("armorhud.option.showWarning", config.isWarningShown(), config::setWarningShown),
                new TypedInputOption<>("armorhud.option.minDuraValue", String.valueOf(config.getMinDurabilityValue()), config::setMinDurabilityValue, this::getInt),
                new SliderOption("armorhud.option.minDuraPercent", config.getMinDurabilityPercentage(), config::setMinDurabilityPercentage, SliderOption.PERCENT_VALUE_TO_TEXT),
                new TypedInputOption<>("armorhud.option.iconBobInterval", String.valueOf(config.getWarningIconBobbingIntervalMs()), config::setWarningIconBobbingIntervalMs, this::getFloat)
        };
    }

    private Optional<Integer> getInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Float> getFloat(String s) {
        try {
            return Optional.of(Float.parseFloat(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
