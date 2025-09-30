package ru.berdinskiybear.armorhud;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.uku3lig.ukulib.config.ConfigManager;
import net.uku3lig.ukulib.utils.Ukutils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class ArmorHudMod implements ClientModInitializer {
    @Getter
    private static final ConfigManager<ArmorHudConfig> manager = ConfigManager.createDefault(ArmorHudConfig.class, "ukus-armor-hud");

    public static final List<Integer> ARMOR_SLOTS = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
            .map(s -> s.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE))
            .toList();

    @Nullable
    public static PlayerEntity getCameraPlayer() {
        return MinecraftClient.getInstance().getCameraEntity() instanceof PlayerEntity player ? player : null;
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
