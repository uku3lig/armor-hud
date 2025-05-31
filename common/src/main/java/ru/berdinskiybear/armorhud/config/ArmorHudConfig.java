package ru.berdinskiybear.armorhud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SelectionListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Arm;
import net.minecraft.util.TranslatableOption;
import org.jetbrains.annotations.NotNull;
import ru.berdinskiybear.armorhud.ArmorHudMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static net.minecraft.text.Text.translatable;

public class ArmorHudConfig implements Serializable {
    public static final ArmorHudConfig CONFIG = new ArmorHudConfig();
    public static final Gson GSON =
            new GsonBuilder().setLenient()
                    .setPrettyPrinting()
                    .registerTypeAdapter(ArmorHudConfig.class, (InstanceCreator<ArmorHudConfig>)type -> CONFIG)
                    .create();
    public static final Path FILE = ArmorHudMod.configDir().resolve(ArmorHudMod.MOD_ID + ".json");

    private ArmorHudConfig() {
        load();
    }

    public boolean enabled = true;
    public Anchor anchor = Anchor.HOTBAR;
    public Side side = Side.LEFT;
    public int offsetX = 0;
    public int offsetY = 0;
    public Style style = Style.HOTBAR;
    public WidgetShown widgetShown = WidgetShown.NOT_EMPTY;
    public OffhandSlotBehavior offhandSlotBehavior = OffhandSlotBehavior.ADHERE;
    public boolean pushBossbars = true;
    public boolean pushStatusEffectIcons = true;
    public boolean pushSubtitles = true;
    public boolean reversed = true;
    public boolean iconsShown = true;
    public boolean warningShown = true;
    public int minDurabilityValue = 5;
    public double minDurabilityPercentage = 0.05;
    public int warningBobIntensity = 3;

    //region getter and setters
    //@formatter:off
    public boolean isDisabled() {
        return !enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public Anchor getAnchor() {
        return anchor;
    }
    public void setAnchor(Anchor anchor) {
        this.anchor = anchor;
    }
    public Side getSide() {
        return side;
    }
    public void setSide(Side side) {
        this.side = side;
    }
    public int getOffsetX() {
        return offsetX;
    }
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }
    public int getOffsetY() {
        return offsetY;
    }
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
    public Style getStyle() {
        return style;
    }
    public void setStyle(Style style) {
        this.style = style;
    }
    public WidgetShown getWidgetShown() {
        return widgetShown;
    }
    public void setWidgetShown(WidgetShown widgetShown) {
        this.widgetShown = widgetShown;
    }
    public OffhandSlotBehavior getOffhandSlotBehavior() {
        return offhandSlotBehavior;
    }
    public void setOffhandSlotBehavior(OffhandSlotBehavior offhandSlotBehavior) {
        this.offhandSlotBehavior = offhandSlotBehavior;
    }
    public boolean isPushBossbars() {
        return pushBossbars;
    }
    public void setPushBossbars(boolean pushBossbars) {
        this.pushBossbars = pushBossbars;
    }
    public boolean isPushStatusEffectIcons() {
        return pushStatusEffectIcons;
    }
    public void setPushStatusEffectIcons(boolean pushStatusEffectIcons) {
        this.pushStatusEffectIcons = pushStatusEffectIcons;
    }
    public boolean isPushSubtitles() {
        return pushSubtitles;
    }
    public void setPushSubtitles(boolean pushSubtitles) {
        this.pushSubtitles = pushSubtitles;
    }
    public boolean isReversed() {
        return reversed;
    }
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }
    public boolean isIconsShown() {
        return iconsShown;
    }
    public void setIconsShown(boolean iconsShown) {
        this.iconsShown = iconsShown;
    }
    public boolean isWarningShown() {
        return warningShown;
    }
    public void setWarningShown(boolean warningShown) {
        this.warningShown = warningShown;
    }
    public int getMinDurabilityValue() {
        return minDurabilityValue;
    }
    public void setMinDurabilityValue(int minDurabilityValue) {
        this.minDurabilityValue = minDurabilityValue;
    }
    public double getMinDurabilityPercentage() {
        return minDurabilityPercentage;
    }
    public void setMinDurabilityPercentage(double minDurabilityPercentage) {
        this.minDurabilityPercentage = minDurabilityPercentage;
    }
    public int getWarningBobIntensity() {
        return warningBobIntensity;
    }
    public void setWarningBobIntensity(int warningBobIntensity) {
        this.warningBobIntensity = warningBobIntensity;
    }
    //@formatter:on
    //endregion

    public void toggleEnabled() {
        setEnabled(!isDisabled());
    }

    public enum Anchor implements TranslatableOption, SelectionListEntry.Translatable {
        TOP_CENTER("armorhud.option.topCenter"),
        TOP("armorhud.option.top"),
        BOTTOM("armorhud.option.bottom"),
        HOTBAR("armorhud.option.hotbar");

        public final String translationKey;

        Anchor(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

        @Override
        public @NotNull String getKey() {
            return translationKey;
        }
    }

    public enum Side implements TranslatableOption, SelectionListEntry.Translatable {
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

        @Override
        public @NotNull String getKey() {
            return getTranslationKey();
        }
    }

    public enum Style implements TranslatableOption, SelectionListEntry.Translatable {
        HOTBAR("armorhud.option.hotbar"),
        ROUNDED_CORNERS("armorhud.option.roundedCorners"),
        ROUNDED("armorhud.option.rounded"),
        NONE("armorhud.option.none");

        public final String translationKey;

        Style(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

        @Override
        public @NotNull String getKey() {
            return translationKey;
        }
    }

    public enum WidgetShown implements TranslatableOption, SelectionListEntry.Translatable {
        ALWAYS("armorhud.option.always"),
        IF_ANY_PRESENT("armorhud.option.ifAnyPresent"),
        NOT_EMPTY("armorhud.option.notEmpty");

        public final String translationKey;

        WidgetShown(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

        @Override
        public @NotNull String getKey() {
            return translationKey;
        }

    }

    public enum OffhandSlotBehavior implements TranslatableOption, SelectionListEntry.Translatable {
        ALWAYS_IGNORE("armorhud.option.alwaysIgnore"),
        ADHERE("armorhud.option.adhere"),
        ALWAYS_LEAVE_SPACE("armorhud.option.alwaysLeaveSpace");

        public final String translationKey;

        OffhandSlotBehavior(String translationKey) {
            this.translationKey = translationKey;
        }

        @Override
        public int getId() {
            return ordinal();
        }

        @Override
        public String getTranslationKey() {
            return translationKey;
        }

        @Override
        public @NotNull String getKey() {
            return translationKey;
        }
    }

    public void load() {
        if (!Files.exists(FILE))
            save();
        else
            try (Reader reader = Files.newBufferedReader(FILE)) {
                GSON.fromJson(reader, ArmorHudConfig.class);
            } catch (IOException e) {
                ArmorHudMod.LOGGER.error("Unable to read config from file!", e);
            } catch (JsonSyntaxException e) {
                ArmorHudMod.LOGGER.error("Error reading config! Reloading from defaults!", e);
                save();
            }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(FILE, StandardOpenOption.CREATE)) {
            GSON.toJson(CONFIG, writer);
        } catch (IOException e) {
            ArmorHudMod.LOGGER.error("Unable to write config to file!", e);
        }
    }

    public Screen createScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(translatable("armorhud.name"))
                .setParentScreen(parent)
                .setSavingRunnable(this::save);
        ConfigEntryBuilder entries = builder.entryBuilder();
        builder.getOrCreateCategory(translatable("armorhud.config"))
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.enabled"), enabled)
                                .setSaveConsumer(this::setEnabled).build())
                .addEntry(
                        entries.startEnumSelector(translatable("armorhud.option.anchor"), Anchor.class, anchor)
                                .setSaveConsumer(this::setAnchor).build())
                .addEntry(
                        entries.startEnumSelector(translatable("armorhud.option.side"), Side.class, side)
                                .setSaveConsumer(this::setSide).build())
                .addEntry(
                        entries.startEnumSelector(translatable("armorhud.option.style"), Style.class, style)
                                .setSaveConsumer(this::setStyle).build())
                .addEntry(
                        entries.startEnumSelector(translatable("armorhud.option.widgetShown"), WidgetShown.class, widgetShown)
                                .setSaveConsumer(this::setWidgetShown).build())
                .addEntry(
                        entries.startEnumSelector(translatable("armorhud.option.offhandSlotBehavior"), OffhandSlotBehavior.class, offhandSlotBehavior)
                                .setSaveConsumer(this::setOffhandSlotBehavior).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.pushBossbars"), pushBossbars)
                                .setSaveConsumer(this::setPushBossbars).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.pushIcons"), pushStatusEffectIcons)
                                .setSaveConsumer(this::setPushStatusEffectIcons).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.pushSubtitles"), pushSubtitles)
                                .setSaveConsumer(this::setPushSubtitles).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.reversed"), reversed)
                                .setSaveConsumer(this::setReversed).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.showIcons"), iconsShown)
                                .setSaveConsumer(this::setIconsShown).build())
                .addEntry(
                        entries.startBooleanToggle(translatable("armorhud.option.showWarning"), warningShown)
                                .setSaveConsumer(this::setWarningShown).build())
                .addEntry(
                        entries.startIntField(translatable("armorhud.option.offsetX"), offsetX)
                                .setSaveConsumer(this::setOffsetX).build())
                .addEntry(
                        entries.startIntField(translatable("armorhud.option.offsetX"), offsetX)
                                .setSaveConsumer(this::setOffsetX).build())
                .addEntry(
                        entries.startIntField(translatable("armorhud.option.offsetY"), offsetY)
                                .setSaveConsumer(this::setOffsetY).build())
                .addEntry(
                        entries.startIntField(translatable("armorhud.option.minDuraValue"), minDurabilityValue)
                                .setSaveConsumer(this::setMinDurabilityValue).build())
                .addEntry(
                        entries.startIntSlider(translatable("armorhud.option.offsetX"), (int) minDurabilityPercentage, 0, 100)
                                .setSaveConsumer(this::setMinDurabilityPercentage).build())
                .addEntry(
                        entries.startIntField(translatable("armorhud.option.iconBobIntensity"), warningBobIntensity)
                                .setSaveConsumer(this::setWarningBobIntensity).build());
        return builder.build();
    }
}
