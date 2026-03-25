package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Optional;

@Mixin(SubtitleOverlay.class)
public class MixinSubtitleOverlay {
    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle
    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I", ordinal = 3))
    public void calculateOffset(GuiGraphicsExtractor graphics, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;
        Optional<Rect2i> rect = ArmorHudMod.getEffectiveWidgetRect(graphics, player);
        if (rect.isEmpty()) return;

        // The subtitles widget is approx 25 pixels above the bottom of the screen
        offsetRef.set(Math.max(rect.get().getHeight() - 25, 0));
    }

    @Inject(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", shift = At.Shift.AFTER, remap = false))
    public void offset(GuiGraphicsExtractor graphics, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        graphics.pose().translate(0.0F, -offsetRef.get());
    }
}
