package ru.berdinskiybear.armorhud.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.util.Arm;
import net.minecraft.util.TranslatableOption;

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
    private Style style = Style.HOTBAR;
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
    private int warningBobIntensity = 3;

    @Getter
    @AllArgsConstructor
    public enum Anchor implements TranslatableOption {
        TOP_CENTER(0, "armorhud.option.topCenter"),
        TOP(1, "armorhud.option.top"),
        BOTTOM(2, "armorhud.option.bottom"),
        HOTBAR(3, "armorhud.option.hotbar");

        private final int id;
        private final String translationKey;
    }

    public enum Side implements TranslatableOption {
        RIGHT,
        LEFT;

        public Arm asArm() {
            return this == LEFT ? Arm.LEFT : Arm.RIGHT;
        }

        @Override
        public int getId() {
            return asArm().getId();
        }

        @Override
        public String getTranslationKey() {
            return asArm().getTranslationKey();
        }
    }

    @Getter
    @AllArgsConstructor
    public enum OffhandSlotBehavior implements TranslatableOption {
        ALWAYS_IGNORE(0, "armorhud.option.alwaysIgnore"),
        ADHERE(1, "armorhud.option.adhere"),
        ALWAYS_LEAVE_SPACE(2, "armorhud.option.alwaysLeaveSpace");

        private final int id;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum WidgetShown implements TranslatableOption {
        ALWAYS(0, "armorhud.option.always"),
        IF_ANY_PRESENT(1, "armorhud.option.ifAnyPresent"),
        NOT_EMPTY(2, "armorhud.option.notEmpty");

        private final int id;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements TranslatableOption {
        HOTBAR(0, "armorhud.option.hotbar"),
        ROUNDED_CORNERS(1, "armorhud.option.roundedCorners"),
        ROUNDED(2, "armorhud.option.rounded");

        private final int id;
        private final String translationKey;
    }
}
