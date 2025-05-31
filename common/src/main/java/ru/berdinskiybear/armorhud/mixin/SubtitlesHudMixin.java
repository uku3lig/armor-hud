package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle (what this makes no sense but ok)
    @Inject(method = "render", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 3))
    public void calculateOffset(DrawContext context, CallbackInfo ci, @Share("offset") LocalIntRef offsetRef) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        List<ItemStack> armorItems = ArmorHudMod.nonEmptyArmor(player);
        int offset = 0;

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            offset += config.getOffsetY();
            if (config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                offset += 10;
                if (config.getWarningBobIntensity() != 0) {
                    offset += Constants.WARNING_OFFSET;
                }
            }
        }

        offsetRef.set(Math.max(offset, 0));
    }

    @ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"), index = 1)
    public float offset(float y, @Share("offset") LocalIntRef offsetRef) {
        return y - offsetRef.get();
    }
}
