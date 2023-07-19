package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {
    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 1)
    public int pushBossBars(int y) {
        final int orig = y;

        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushBossbars() || config.getAnchor() != ArmorHudConfig.Anchor.TOP_CENTER)
            return y;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return y;

        List<ItemStack> armorItems = player.getInventory().armor.stream().filter(s -> !s.isEmpty()).toList();

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            y += 22 + config.getOffsetY();
            if (config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                y += 10;
                if (config.getWarningBobIntensity() != 0) {
                    y += 7;
                }
            }
        }

        return Math.max(y, orig);
    }
}
