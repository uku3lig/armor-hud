package ru.berdinskiybear.armorhud.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArmorHudConfig implements Serializable {
    private boolean enabled = true;
    private Anchor anchor = Anchor.HOTBAR;
    private Side side = Side.LEFT;
    private int offsetX = 0;
    private int offsetY = 0;
    private Style style = Style.STYLE_1_E;
    private WidgetShown widgetShown = WidgetShown.NOT_EMPTY;
    private OffhandSlotBehavior offhandSlotBehavior = OffhandSlotBehavior.ADHERE;
    private boolean pushBossbars = true;
    private boolean pushStatusEffectIcons = true;
    private boolean pushSubtitles = true;
    private boolean reversed = true;
    private boolean iconsShown = true;
    private boolean warningShown = true;
    private int minDurabilityValue = 5;
    private double minDurabilityPercentage = 0.05;
    private float warningIconBobbingIntervalMs = 2000.0f;

    public ArmorHudConfig(ArmorHudConfig original) {
        this.enabled = original.enabled;
        this.anchor = original.anchor;
        this.side = original.side;
        this.offsetX = original.offsetX;
        this.offsetY = original.offsetY;
        this.style = original.style;
        this.widgetShown = original.widgetShown;
        this.offhandSlotBehavior = original.offhandSlotBehavior;
        this.pushBossbars = original.pushBossbars;
        this.pushStatusEffectIcons = original.pushStatusEffectIcons;
        this.pushSubtitles = original.pushSubtitles;
        this.reversed = original.reversed;
        this.iconsShown = original.iconsShown;
        this.warningShown = original.warningShown;
        this.minDurabilityValue = original.minDurabilityValue;
        this.minDurabilityPercentage = original.minDurabilityPercentage;
        this.warningIconBobbingIntervalMs = original.warningIconBobbingIntervalMs;
    }

    public enum Anchor {
        TOP_CENTER,
        TOP,
        BOTTOM,
        HOTBAR
    }

    public enum Side {
        RIGHT,
        LEFT
    }

    public enum OffhandSlotBehavior {
        ALWAYS_IGNORE,
        ADHERE,
        ALWAYS_LEAVE_SPACE
    }

    public enum WidgetShown {
        ALWAYS,
        IF_ANY_PRESENT,
        NOT_EMPTY
    }

    public enum Style {
        STYLE_1_E,
        STYLE_1_H,
        STYLE_1_S,
        STYLE_2_E,
        STYLE_2_H,
        STYLE_2_S,
        STYLE_3
    }
}
