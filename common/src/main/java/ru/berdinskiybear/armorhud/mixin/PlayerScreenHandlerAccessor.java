package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PlayerScreenHandler.class)
public interface PlayerScreenHandlerAccessor {
    @Accessor
    static Map<EquipmentSlot, Identifier> getEMPTY_ARMOR_SLOT_TEXTURES() {
        throw new AssertionError();
    }

    @Accessor
    static EquipmentSlot[] getEQUIPMENT_SLOT_ORDER() {
        throw new AssertionError();
    }
}
