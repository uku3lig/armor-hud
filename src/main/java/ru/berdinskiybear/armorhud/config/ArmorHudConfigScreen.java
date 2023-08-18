package ru.berdinskiybear.armorhud.config;

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.TranslatableOption;
import net.uku3lig.ukulib.config.screen.AbstractConfigScreen;
import ru.berdinskiybear.armorhud.ArmorHudMod;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.function.Consumer;

public class ArmorHudConfigScreen extends AbstractConfigScreen<ArmorHudConfig> {
    protected ArmorHudConfigScreen(Screen parent) {
        super(parent, Text.of("armorhud.config"), ArmorHudMod.getManager());
    }

    @Override
    protected SimpleOption<?>[] getOptions(ArmorHudConfig config) {
        int width = MinecraftClient.getInstance().getWindow().getWidth();
        int height = MinecraftClient.getInstance().getWindow().getHeight();

        return new SimpleOption[]{
                SimpleOption.ofBoolean("armorhud.option.enabled", config.isEnabled(), config::setEnabled),
                ofTranslatableEnum("armorhud.option.anchor", ArmorHudConfig.Anchor.class, config.getAnchor(), config::setAnchor),
                ofTranslatableEnum("armorhud.option.side", ArmorHudConfig.Side.class, config.getSide(), config::setSide),
                slider("armorhud.option.offsetX", config.getOffsetX(), width, config::setOffsetX),
                slider("armorhud.option.offsetY", config.getOffsetY(), height, config::setOffsetY),
                ofTranslatableEnum("armorhud.option.style", ArmorHudConfig.Style.class, config.getStyle(), config::setStyle),
                ofTranslatableEnum("armorhud.option.widgetShown", ArmorHudConfig.WidgetShown.class, config.getWidgetShown(), config::setWidgetShown),
                ofTranslatableEnum("armorhud.option.offhandSlotBehavior", ArmorHudConfig.OffhandSlotBehavior.class, config.getOffhandSlotBehavior(), config::setOffhandSlotBehavior),
                SimpleOption.ofBoolean("armorhud.option.pushBossbars", config.isPushBossbars(), config::setPushBossbars),
                SimpleOption.ofBoolean("armorhud.option.pushIcons", config.isPushStatusEffectIcons(), config::setPushStatusEffectIcons),
                SimpleOption.ofBoolean("armorhud.option.pushSubtitles", config.isPushSubtitles(), config::setPushSubtitles),
                SimpleOption.ofBoolean("armorhud.option.reversed", config.isReversed(), config::setReversed),
                SimpleOption.ofBoolean("armorhud.option.showIcons", config.isIconsShown(), config::setIconsShown),
                SimpleOption.ofBoolean("armorhud.option.showWarning", config.isWarningShown(), config::setWarningShown),
                slider("armorhud.option.minDuraValue", config.getMinDurabilityValue(), 2000, config::setMinDurabilityValue),
                slider("armorhud.option.minDuraPercent", (int) (config.getMinDurabilityPercentage() * 100), 100, v -> config.setMinDurabilityPercentage(v / 100.0)),
                slider("armorhud.option.iconBobIntensity", config.getWarningBobIntensity(), 15, config::setWarningBobIntensity)
        };
    }

    private <T extends Enum<T> & TranslatableOption> SimpleOption<T> ofTranslatableEnum(String key, Class<T> klass, T value, Consumer<T> setter) {
        return new SimpleOption<>(key, SimpleOption.emptyTooltip(), (text, val) -> Text.translatable(val.getTranslationKey()),
                new SimpleOption.PotentialValuesBasedCallbacks<>(new ArrayList<>(EnumSet.allOf(klass)), Codec.STRING.xmap(s -> Enum.valueOf(klass, s), Enum::name)),
                value, setter);
    }

    private SimpleOption<Integer> slider(String key, int value, int max, Consumer<Integer> setter) {
        return new SimpleOption<>(key, SimpleOption.emptyTooltip(), GameOptions::getGenericValueText,
                new SimpleOption.ValidatingIntSliderCallbacks(0, max), value, setter);
    }
}
