package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    public void playBreakSound(CallbackInfo ci) {
        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (ArmorHudMod.getManager().getConfig().isPlayBreakSound() && player != null && ArmorHudMod.shouldPlayBreakSound(player)) {
            player.playSound(SoundEvent.of(ArmorHudMod.ARMOR_BREAKING_SOUND));
        }
    }
}
