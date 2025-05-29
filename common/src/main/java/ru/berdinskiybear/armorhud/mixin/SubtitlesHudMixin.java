package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    @Unique
    private int armorHud$offset = 0;

    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle (what this makes no sense but ok)
    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 4))
    public void calculateOffset(DrawContext context, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        List<ItemStack> armorItems = player.getInventory().armor.stream().filter(s -> !s.isEmpty()).toList();

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            this.armorHud$offset += config.getOffsetY();
            if (config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                this.armorHud$offset += 10;
                if (config.getWarningBobIntensity() != 0) {
                    this.armorHud$offset += 7;
                }
            }
        }

        this.armorHud$offset = Math.max(this.armorHud$offset, 0);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", shift = At.Shift.AFTER))
    public void offset(DrawContext context, CallbackInfo ci) {
        context.getMatrices().translate(0.0F, -((float) this.armorHud$offset), 0.0F);
    }
}
