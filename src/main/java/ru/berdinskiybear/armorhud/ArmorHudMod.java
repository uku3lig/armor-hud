package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class ArmorHudMod implements ClientModInitializer {
    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, "ukus-armor-hud");

    public static final int STEP = 20;
    public static final int WIDTH = 22;
    public static final int HEIGHT = 22;
    public static final int HOTBAR_OFFSET = 98;
    public static final int OFFHAND_OFFSET = 29;
    public static final int ATTACK_INDICATOR_OFFSET = 23;
    public static final int WARNING_OFFSET = 7;

    public static final List<Integer> ARMOR_SLOTS = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
            .map(s -> s.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE))
            .toList();

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

        final int verticalMultiplier = switch (config.getAnchor()) {
            case TOP, TOP_CENTER -> 1;
            case BOTTOM, HOTBAR -> -1;
        };

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
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

        final int textureWidth = WIDTH + ((armorItems.size() - 1) * STEP);
        final int widgetWidth = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? WIDTH : textureWidth;
        final int widgetHeight = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? textureWidth : HEIGHT;

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> context.getScaledWindowWidth() / 2 - (widgetWidth / 2);
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR ->
                    context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
        };

        final int armorWidgetY = config.getOffsetY() * verticalMultiplier + switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> context.getScaledWindowHeight() - widgetHeight;
            case TOP, TOP_CENTER -> 0;
        };

        return Optional.of(new Rect2i(armorWidgetX, armorWidgetY, widgetWidth, widgetHeight));
    }

    /**
     * Returns the effective bounding box, <strong>including</strong> "external" information like warning icon
     */
    public static Optional<Rect2i> getEffectiveWidgetRect(DrawContext context, PlayerEntity player) {
        ArmorHudConfig config = manager.getConfig();
        Optional<Rect2i> rect = getWidgetRect(context, player);
        if (rect.isEmpty()) return Optional.empty();
        // TODO should probably extend the bbox horizontally too
        if (config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) return rect;
        Rect2i unwrapped = rect.get();

        if (config.isWarningShown()) {
            int additionalHeight = 10 + (config.getWarningBobIntensity() / 2);
            unwrapped.setHeight(unwrapped.getHeight() + additionalHeight);
            if (config.getAnchor() == ArmorHudConfig.Anchor.BOTTOM || config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR) {
                unwrapped.setY(unwrapped.getY() - additionalHeight);
            }
        }

        return Optional.of(unwrapped);
    }

    public static List<ItemStack> getArmorItems(PlayerEntity player) {
        Stream<ItemStack> items = ARMOR_SLOTS.stream().map(i -> player.getInventory().getStack(i));
        items = switch (manager.getConfig().getWidgetShown()) {
            case ALWAYS, IF_ANY_PRESENT -> items;
            case NOT_EMPTY -> items.filter(s -> !s.isEmpty());
            case DAMAGED_PIECES -> items.filter(ArmorHudMod::shouldShowWarning);
        };
        List<ItemStack> itemList = items.toList();

        if (manager.getConfig().getWidgetShown() == ArmorHudConfig.WidgetShown.IF_ANY_PRESENT
                && itemList.stream().allMatch(ItemStack::isEmpty)) {
            return Collections.emptyList();
        } else {
            return itemList;
        }
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
    public void onInitializeClient() {
        Ukutils.registerToggleBind(new KeyBinding("armorhud.keybind.toggle", GLFW.GLFW_KEY_UNKNOWN, KeyBinding.Category.create(Identifier.of("ukus-armor-hud", "key"))),
                () -> manager.getConfig().isEnabled(), b -> manager.getConfig().setEnabled(b), Text.translatable("armorhud.keybind.toggle.msg"));
    }
}
