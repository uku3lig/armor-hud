package ru.berdinskiybear.armorhud.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.world.entity.HumanoidArm;
import net.uku3lig.ukulib.config.option.StringTranslatable;

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
    private Orientation orientation = Orientation.HORIZONTAL;
    private WidgetShown widgetShown = WidgetShown.NOT_EMPTY;
    private OffhandSlotBehavior offhandSlotBehavior = OffhandSlotBehavior.ADHERE;
    private DurabilityDisplay durabilityDisplay = DurabilityDisplay.BAR;
    private boolean pushBossbars = true;
    private boolean pushStatusEffectIcons = true;
    private boolean pushSubtitles = true;
    private boolean reversed = false;
    private boolean iconsShown = true;
    private boolean warningShown = true;
    private boolean playBreakSound = true;
    private int minDurabilityValue = 20;
    private double minDurabilityPercentage = 0.1;
    private int warningBobIntensity = 3;

    @Getter
    @AllArgsConstructor
    public enum Anchor implements StringTranslatable {
        TOP_CENTER("top_center", "armorhud.option.topCenter"),
        TOP("top", "armorhud.option.top"),
        BOTTOM("bottom", "armorhud.option.bottom"),
        HOTBAR("hotbar", "armorhud.option.hotbar");

        private final String name;
        private final String translationKey;

        public boolean isTop() {
            return this == TOP || this == TOP_CENTER;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Side implements StringTranslatable {
        LEFT("left", "options.mainHand.left"),
        RIGHT("right", "options.mainHand.right");

        private final String name;
        private final String translationKey;

        public Side getOpposite() {
            return this == LEFT ? RIGHT : LEFT;
        }

        public HumanoidArm asArm() {
            return this == LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum OffhandSlotBehavior implements StringTranslatable {
        ALWAYS_IGNORE("always_ignore", "armorhud.option.alwaysIgnore"),
        ADHERE("adhere", "armorhud.option.adhere"),
        ALWAYS_LEAVE_SPACE("always_leave_space", "armorhud.option.alwaysLeaveSpace");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum WidgetShown implements StringTranslatable {
        ALWAYS("always", "armorhud.option.always"),
        IF_ANY_PRESENT("if_any_present", "armorhud.option.ifAnyPresent"),
        NOT_EMPTY("not_empty", "armorhud.option.notEmpty"),
        DAMAGED_PIECES("damaged_pieces", "armorhud.option.damagedPieces");

        private final String name;
        private final String translationKey;

        public boolean shouldDrawEmptySlots() {
            return this == ALWAYS || this == IF_ANY_PRESENT;
        }
    }

    @Getter
    @AllArgsConstructor
    public enum Style implements StringTranslatable {
        HOTBAR("hotbar", "armorhud.option.hotbar"),
        ROUNDED_CORNERS("rounded_corners", "armorhud.option.roundedCorners"),
        ROUNDED("rounded", "armorhud.option.rounded"),
        NONE("none", "armorhud.option.none");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum Orientation implements StringTranslatable {
        HORIZONTAL("horizontal", "armorhud.option.horizontal"),
        VERTICAL("vertical", "armorhud.option.vertical");

        private final String name;
        private final String translationKey;
    }

    @Getter
    @AllArgsConstructor
    public enum DurabilityDisplay implements StringTranslatable {
        BAR("bar", "armorhud.option.bar"),
        NUMERIC("numeric", "armorhud.option.numeric"),
        PERCENTAGE("percentage", "armorhud.option.percentage");

        private final String name;
        private final String translationKey;
    }
}
