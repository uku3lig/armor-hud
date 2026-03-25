package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Optional;

@Mixin(BossHealthOverlay.class)
public class MixinBossHealthOverlay {
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void calculateOffset(GuiGraphicsExtractor graphics, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;
        Optional<Rect2i> rect = ArmorHudMod.getEffectiveWidgetRect(graphics, player);
        if (rect.isEmpty()) return;

        offsetRef.set(rect.get().getY() + rect.get().getHeight());
    }

    @ModifyVariable(method = "extractRenderState", at = @At("STORE"), name = "yOffset")
    public int pushBossBars(int y, @Share("offset") LocalIntRef offsetRef) {
        return y + offsetRef.get();
    }
}
