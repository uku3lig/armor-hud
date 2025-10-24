package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Optional;

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    @Unique
    private int offset = 0;

    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 3))
    public void calculateOffset(DrawContext context, CallbackInfo ci) {
        this.offset = 0;
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM
                || config.getSide() != Arm.RIGHT) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;
        Optional<Rect2i> rect = ArmorHudMod.getEffectiveWidgetRect(context, player);
        if (rect.isEmpty()) return;

        this.offset = rect.get().getHeight();
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;translate(FF)Lorg/joml/Matrix3x2f;", shift = At.Shift.AFTER, remap = false))
    public void offset(DrawContext context, CallbackInfo ci) {
        context.getMatrices().translate(0.0F, -this.offset);
    }
}
