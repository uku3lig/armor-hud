package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "tick", at = @At("RETURN"))
    public void playBreakSound(CallbackInfo ci) {
        Player player = ArmorHudMod.getCameraPlayer();
        if (ArmorHudMod.getManager().getConfig().isPlayBreakSound() && player != null && ArmorHudMod.shouldPlayBreakSound(player)) {
            player.makeSound(ArmorHudMod.ARMOR_BREAKING_SOUND);
        }
    }
}
