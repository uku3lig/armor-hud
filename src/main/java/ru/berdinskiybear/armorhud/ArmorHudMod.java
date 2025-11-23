package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import ru.berdinskiybear.armorhud.mixin.PlayerScreenHandlerAccessor;

import java.util.*;
import java.util.stream.Stream;

public final class ArmorHudMod implements ModInitializer {
    public static final String MOD_ID = "ukus-armor-hud";

    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, MOD_ID);

    public static final int STEP = 20;
    public static final int SIZE = 22;
    public static final int HOTBAR_OFFSET = 98;
    public static final int OFFHAND_OFFSET = 29;
    public static final int ATTACK_INDICATOR_OFFSET = 23;
    public static final int WARNING_SIZE = 8;

    public static final SoundEvent ARMOR_BREAKING_SOUND = SoundEvent.of(Identifier.of(MOD_ID, "armor_breaking"));

    public static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = PlayerScreenHandlerAccessor.getEQUIPMENT_SLOT_ORDER();

    private static final List<ItemStack> lastStacks = new ArrayList<>(Collections.nCopies(EQUIPMENT_SLOT_ORDER.length, ItemStack.EMPTY));

    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
    }

    /**
     * Returns the bounding box of the widget itself, <strong>excluding</strong> "external" information like warning icon
     */
    public static Optional<Rect2i> getWidgetRect(DrawContext context, PlayerEntity player) {
        ArmorHudConfig config = manager.getConfig();
        List<ItemStack> armorItems = getArmorItems(player);

        if (armorItems.isEmpty()) {
            return Optional.empty();
        }

        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final int sideMultiplier, sideOffsetMultiplier;
        if ((config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR && config.getSide() == Arm.LEFT)
                || (config.getAnchor() != ArmorHudConfig.Anchor.HOTBAR && config.getSide() == Arm.RIGHT)) {
            sideMultiplier = -1;
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1;
            sideOffsetMultiplier = 0;
        }

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
            // FIXME probably need to account for both?
            case ALWAYS_LEAVE_SPACE -> Math.max(OFFHAND_OFFSET, ATTACK_INDICATOR_OFFSET);
            case ADHERE -> {
                if (player.getMainArm().getOpposite() == config.getSide()) {
                    if (!player.getOffHandStack().isEmpty()) {
                        yield OFFHAND_OFFSET;
                    } else if (MinecraftClient.getInstance().options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                        yield ATTACK_INDICATOR_OFFSET;
                    }
                }

                yield 0;
            }
        };

        final int textureWidth = SIZE + ((armorItems.size() - 1) * STEP);
        final int widgetWidth = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? SIZE : textureWidth;
        final int widgetHeight = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? textureWidth : SIZE;

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> (context.getScaledWindowWidth() - widgetWidth) / 2;
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR ->
                    context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
        };

        final int armorWidgetY = switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> context.getScaledWindowHeight() - widgetHeight - config.getOffsetY();
            case TOP, TOP_CENTER -> config.getOffsetY();
        };

        return Optional.of(new Rect2i(armorWidgetX, armorWidgetY, widgetWidth, widgetHeight));
    }

    /**
     * Returns the effective bounding box, <strong>including</strong> "external" information like warning icon
     */
    public static Optional<Rect2i> getEffectiveWidgetRect(DrawContext context, PlayerEntity player) {
        ArmorHudConfig config = manager.getConfig();

        return getWidgetRect(context, player).map(rect -> {
            // TODO should probably extend the bbox horizontally too
            if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                int additionalHeight = 0;

                if (config.isWarningShown()) {
                    additionalHeight += WARNING_SIZE + 2 + (config.getWarningBobIntensity() / 2);
                }

                if (config.getDurabilityDisplay() == ArmorHudConfig.DurabilityDisplay.NUMERIC) {
                    additionalHeight += MinecraftClient.getInstance().textRenderer.fontHeight;
                }

                rect.setHeight(rect.getHeight() + additionalHeight);
                if (!config.getAnchor().isTop()) {
                    rect.setY(rect.getY() - additionalHeight);
                }
            }

            return rect;
        });
    }

    public static List<ItemStack> getArmorItems(PlayerEntity player) {
        Stream<ItemStack> items = Arrays.stream(EQUIPMENT_SLOT_ORDER).map(player::getEquippedStack);
        items = switch (manager.getConfig().getWidgetShown()) {
            case ALWAYS -> items;
            case IF_ANY_PRESENT -> {
                List<ItemStack> itemList = items.toList();
                yield itemList.stream().allMatch(ItemStack::isEmpty) ? Stream.of() : itemList.stream();
            }
            case NOT_EMPTY -> items.filter(s -> !s.isEmpty());
            case DAMAGED_PIECES -> items.filter(ArmorHudMod::shouldShowWarning);
        };

        return items.toList();
    }

    public static boolean shouldPlayBreakSound(PlayerEntity player) {
        for (int i = 0; i < EQUIPMENT_SLOT_ORDER.length; i++) {
            EquipmentSlot slot = EQUIPMENT_SLOT_ORDER[i];
            ItemStack current = player.getEquippedStack(slot);
            ItemStack last = lastStacks.set(i, current);
            if (last.getDamage() != current.getDamage() && shouldShowWarning(current)) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldShowWarning(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageable()) return false;

        final int damage = stack.getDamage();
        final int maxDamage = stack.getMaxDamage();
        double percentage = 1.0 - ((double) damage / maxDamage);

        return percentage <= manager.getConfig().getMinDurabilityPercentage()
                || maxDamage - damage <= manager.getConfig().getMinDurabilityValue();
    }

    @Override
    public void onInitialize() {
        Ukutils.registerToggleBind(new KeyBinding("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.create(Identifier.of(MOD_ID, "key"))),
                () -> manager.getConfig().isEnabled(), b -> manager.getConfig().setEnabled(b), Text.translatable("armorhud.keybind.toggle.msg"));

        Registry.register(Registries.SOUND_EVENT, ARMOR_BREAKING_SOUND.id(), ARMOR_BREAKING_SOUND);
    }
}
