package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Optional;

import static ru.berdinskiybear.armorhud.ArmorHudMod.*;

@Mixin(Hud.class)
public class MixinHud {
    @Inject(method = "extractEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        Player player = getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = getEffectiveWidgetRect(graphics, player);
        if (rect.isEmpty()) return;

        shiftRef.set(rect.get().getY() + rect.get().getHeight());
    }

    @ModifyVariable(method = "extractEffects", at = @At(value = "STORE"), name = "y")
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}
