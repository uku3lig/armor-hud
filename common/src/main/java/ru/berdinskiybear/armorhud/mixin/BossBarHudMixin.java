package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "intValue=12"))
    public int pushBossBars(int y) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return y;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return y;

        final int orig = y;
        List<ItemStack> armorItems = ArmorHudMod.nonEmptyArmor(player);

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            y += Constants.SIZE + config.getOffsetY();
            if (config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                y += 10;
                if (config.getWarningBobIntensity() != 0)
                    y += Constants.WARNING_OFFSET;
            }
        }

        return Math.max(y, orig);
    }
}
