package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import java.util.ArrayList;
import java.util.List;

@Mixin(BossBarHud.class)
public class BossBarHudMixin {

    @Shadow @Final private MinecraftClient client;

    private final List<ItemStack> armorHud_armorItems = new ArrayList<>(4);

    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 1)
    public int anInt(int a) {
        ArmorHudConfig currentConfig = ArmorHudMod.getManager().getConfig();
        if (currentConfig.isEnabled() && currentConfig.isPushBossbars()) {
            int add = 0;
            if (currentConfig.getAnchor() == ArmorHudConfig.Anchor.TOP_CENTER) {
                int amount = 0;
                PlayerEntity playerEntity = this.getCameraPlayer();
                if (playerEntity != null) {
                    this.armorHud_armorItems.clear();
                    for (ItemStack itemStack : playerEntity.getInventory().armor) {
                        if (!itemStack.isEmpty())
                            amount++;
                        if (!itemStack.isEmpty() || currentConfig.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY)
                            this.armorHud_armorItems.add(itemStack);
                    }

                    if (amount > 0 || currentConfig.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
                        add += 22 + currentConfig.getOffsetY();
                        if (currentConfig.isWarningShown() && this.armorHud_armorItems.stream().anyMatch((ItemStack itemStack) -> {
                            if (itemStack.isDamageable()) {
                                final int damage = itemStack.getDamage();
                                final int maxDamage = itemStack.getMaxDamage();
                                return ((1.0F - ((float) damage) / ((float) maxDamage) <= currentConfig.getMinDurabilityPercentage()) || (maxDamage - damage <= currentConfig.getMinDurabilityValue()));
                            }
                            return false;
                        })) {
                            add += 2 + 8;
                            if (currentConfig.getWarningIconBobbingIntervalMs() != 0.0F) {
                                add += 7;
                            }
                        }
                    }
                }
            }
            return a + Math.max(add, 0);
        } else
            return a;
    }

    private PlayerEntity getCameraPlayer() {
        return !(this.client.getCameraEntity() instanceof PlayerEntity) ? null : (PlayerEntity) this.client.getCameraEntity();
    }
}
