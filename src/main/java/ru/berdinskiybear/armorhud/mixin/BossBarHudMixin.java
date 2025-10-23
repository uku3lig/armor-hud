package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.Optional;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @Unique
    private int offset = 0;

    @Inject(method = "render", at = @At("HEAD"))
    public void calculateOffset(DrawContext context, CallbackInfo ci) {
        this.offset = 0;

        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;
        Optional<Rect2i> rect = ArmorHudMod.getWidgetRect(context, player);
        if (rect.isEmpty()) return;


        this.offset = rect.get().getY() + rect.get().getHeight();
        if (config.isWarningShown() && config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
            this.offset += 10 + config.getWarningBobIntensity();
        }
    }

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 1)
    public int pushBossBars(int y) {
        return y + this.offset;
    }
}
