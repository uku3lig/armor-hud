package ru.berdinskiybear.armorhud.config;

import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.screens.Screen;
import net.uku3lig.ukulib.config.option.CyclingOption;
import net.uku3lig.ukulib.config.option.SliderOption;
import net.uku3lig.ukulib.config.option.TypedInputOption;
import net.uku3lig.ukulib.config.option.WidgetCreator;
import net.uku3lig.ukulib.config.option.widget.ButtonTab;
import net.uku3lig.ukulib.config.screen.TabbedConfigScreen;
import ru.berdinskiybear.armorhud.ArmorHudMod;

import java.util.Optional;

public class ArmorHudConfigScreen extends TabbedConfigScreen<ArmorHudConfig> {
    protected ArmorHudConfigScreen(Screen parent) {
        super("armorhud.config", parent, ArmorHudMod.getManager());
    }

    @Override
    protected Tab[] getTabs(ArmorHudConfig config) {
        return new Tab[] {new StyleTab(), new PositionTab(), new WarningTab()};
    }

    private static Optional<Integer> getInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static class StyleTab extends ButtonTab<ArmorHudConfig> {
        protected StyleTab() {
            super("armorhud.config.style", ArmorHudMod.getManager());
        }

        @Override
        protected WidgetCreator[] getWidgets(ArmorHudConfig config) {
            return new WidgetCreator[] {
                    CyclingOption.ofBoolean("armorhud.option.enabled", config.isEnabled(), config::setEnabled),
                    CyclingOption.ofTranslatableEnum("armorhud.option.style", ArmorHudConfig.Style.class, config.getStyle(), config::setStyle),
                    CyclingOption.ofTranslatableEnum("armorhud.option.widgetShown", ArmorHudConfig.WidgetShown.class, config.getWidgetShown(), config::setWidgetShown),
                    CyclingOption.ofTranslatableEnum("armorhud.option.offhandSlotBehavior", ArmorHudConfig.OffhandSlotBehavior.class, config.getOffhandSlotBehavior(), config::setOffhandSlotBehavior),
                    CyclingOption.ofTranslatableEnum("armorhud.option.durabilityDisplay", ArmorHudConfig.DurabilityDisplay.class, config.getDurabilityDisplay(), config::setDurabilityDisplay),
                    CyclingOption.ofBoolean("armorhud.option.reversed", config.isReversed(), config::setReversed),
                    CyclingOption.ofBoolean("armorhud.option.showIcons", config.isIconsShown(), config::setIconsShown),
                    CyclingOption.ofBoolean("armorhud.option.offhandDura", config.isOffHandDurability(), config::setOffHandDurability),
                    CyclingOption.ofBoolean("armorhud.option.mainHandDura", config.isMainHandDurability(), config::setMainHandDurability),
            };
        }
    }

    public static class PositionTab extends ButtonTab<ArmorHudConfig> {
        protected PositionTab() {
            super("armorhud.config.position", ArmorHudMod.getManager());
        }

        @Override
        protected WidgetCreator[] getWidgets(ArmorHudConfig config) {
            return new WidgetCreator[] {
                    CyclingOption.ofTranslatableEnum("armorhud.option.anchor", ArmorHudConfig.Anchor.class, config.getAnchor(), config::setAnchor),
                    CyclingOption.ofTranslatableEnum("armorhud.option.side", ArmorHudConfig.Side.class, config.getSide(), config::setSide),
                    CyclingOption.ofTranslatableEnum("armorhud.option.orientation", ArmorHudConfig.Orientation.class, config.getOrientation(), config::setOrientation),
                    CyclingOption.ofBoolean("armorhud.option.pushBossbars", config.isPushBossbars(), config::setPushBossbars),
                    CyclingOption.ofBoolean("armorhud.option.pushIcons", config.isPushStatusEffectIcons(), config::setPushStatusEffectIcons),
                    CyclingOption.ofBoolean("armorhud.option.pushSubtitles", config.isPushSubtitles(), config::setPushSubtitles),
                    new TypedInputOption<>("armorhud.option.offsetX", String.valueOf(config.getOffsetX()), config::setOffsetX, ArmorHudConfigScreen::getInt),
                    new TypedInputOption<>("armorhud.option.offsetY", String.valueOf(config.getOffsetY()), config::setOffsetY, ArmorHudConfigScreen::getInt),
            };
        }
    }

    public static class WarningTab extends ButtonTab<ArmorHudConfig> {
        protected WarningTab() {
            super("armorhud.config.warning", ArmorHudMod.getManager());
        }

        @Override
        protected WidgetCreator[] getWidgets(ArmorHudConfig config) {
            return new WidgetCreator[] {
                    CyclingOption.ofBoolean("armorhud.option.playBreakSound", config.isPlayBreakSound(), config::setPlayBreakSound),
                    CyclingOption.ofBoolean("armorhud.option.showWarning", config.isWarningShown(), config::setWarningShown),
                    new TypedInputOption<>("armorhud.option.iconBobIntensity", String.valueOf(config.getWarningBobIntensity()), config::setWarningBobIntensity, ArmorHudConfigScreen::getInt),
                    new TypedInputOption<>("armorhud.option.minDuraValue", String.valueOf(config.getMinDurabilityValue()), config::setMinDurabilityValue, ArmorHudConfigScreen::getInt),
                    new SliderOption("armorhud.option.minDuraPercent", config.getMinDurabilityPercentage(), config::setMinDurabilityPercentage, SliderOption.PERCENT_VALUE_TO_TEXT),
            };
        }
    }
}
