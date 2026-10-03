package ru.berdinskiybear.armorhud.fabric.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.util.profiling.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudRenderer;

import static ru.berdinskiybear.armorhud.ArmorHudMod.MOD_ID;

@Mixin(Hud.class)
public class MixinHud {
    @Inject(method = "extractItemHotbar", at = @At("TAIL"))
    public void renderArmorHud(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Profiler.get().push(MOD_ID);
        ArmorHudRenderer.drawArmorHud(graphics, deltaTracker);
        Profiler.get().pop();
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    public void renderHands(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        ArmorHudRenderer.renderMainAndOffhand(graphics);
    }
}
