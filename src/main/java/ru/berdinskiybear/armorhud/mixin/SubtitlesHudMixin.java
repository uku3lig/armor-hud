package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.SubtitlesHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

@Mixin(SubtitlesHud.class)
public class SubtitlesHudMixin {
    @Shadow @Final private MinecraftClient client;

    @Unique
    private int offset = 0;

    // doing the calculation here allows to calculate only once, since there is one translate call for each subtitle
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/font/TextRenderer;getWidth(Ljava/lang/String;)I", ordinal = 3, shift = At.Shift.BY, by = 4))
    public void calculateOffset(DrawContext context, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushSubtitles() || config.getAnchor() != ArmorHudConfig.Anchor.BOTTOM
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        ClientPlayerEntity player = this.client.player;
        if (player == null) return;

        List<ItemStack> armorItems = player.getInventory().armor.stream().filter(s -> !s.isEmpty()).toList();

        if (!armorItems.isEmpty() || config.getWidgetShown() == ArmorHudConfig.WidgetShown.ALWAYS) {
            this.offset += config.getOffsetY();
            if (config.isWarningShown() && armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
                this.offset += 10;
                if (config.getWarningIconBobbingIntervalMs() != 0.0F) {
                    this.offset += 7;
                }
            }
        }

        this.offset = Math.max(this.offset, 0);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V", shift = At.Shift.AFTER))
    public void offset(DrawContext context, CallbackInfo ci) {
        context.getMatrices().translate(0.0F, -((float) this.offset), 0.0F);
    }
}
