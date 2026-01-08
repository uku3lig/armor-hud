package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(InventoryMenu.class)
public interface InventoryMenuAccessor {
    @Accessor
    static Map<EquipmentSlot, Identifier> getTEXTURE_EMPTY_SLOTS() {
        throw new UnsupportedOperationException();
    }

    @Accessor
    static EquipmentSlot[] getSLOT_IDS() {
        throw new UnsupportedOperationException();
    }
}
