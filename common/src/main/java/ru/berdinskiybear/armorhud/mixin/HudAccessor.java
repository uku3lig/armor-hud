package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Hud.class)
public interface HudAccessor {
    @Accessor
    static Identifier getHOTBAR_SPRITE() {
        throw new AssertionError();
    }

    @Accessor
    static Identifier getHOTBAR_OFFHAND_LEFT_SPRITE() {
        throw new AssertionError();
    }

    @Accessor
    RandomSource getRandom();

    @Invoker("extractSlot")
    void armorhud$extractSlot(GuiGraphicsExtractor graphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int seed);
}
