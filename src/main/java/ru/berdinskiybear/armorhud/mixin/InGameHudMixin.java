package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profilers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;
import java.util.Optional;

import static ru.berdinskiybear.armorhud.ArmorHudMod.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private Random random;

    @Unique
    private static final Identifier WARNING_TEXTURE = Identifier.of("ukus-armor-hud", "warn.png");

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "renderHotbar", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        Profilers.get().push("ukus-armor-hud");

        // this was extracted to a different method to be able to return whenever I want
        // without messing up the profiler
        drawArmorHud(context, tickCounter);

        // pop this out of profiler
        Profilers.get().pop();
    }

    @Unique
    private void drawArmorHud(DrawContext context, RenderTickCounter tickCounter) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled()) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        final Optional<Rect2i> rect = ArmorHudMod.getWidgetRect(context, player);
        // return if there is nothing to draw
        if (rect.isEmpty()) return;

        // fetch armor items
        List<ItemStack> armorItems = ArmorHudMod.getArmorItems(player);
        if (config.isReversed()) {
            armorItems = armorItems.reversed();
        }

        final int textureWidth = SIZE + ((armorItems.size() - 1) * STEP);

        // here I draw the slots
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(rect.get().getX(), rect.get().getY());

        if (config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
            context.getMatrices().rotate(MathHelper.HALF_PI).translate(0, -SIZE);
        }

        switch (config.getStyle()) {
            case HOTBAR -> {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 0, 0, 0, 0, textureWidth - 3, SIZE);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, SIZE);
            }
            case ROUNDED_CORNERS -> {
                if (armorItems.size() > 1) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, 3, SIZE);
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 3, 0, 3, 0, textureWidth - 6, SIZE);
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, SIZE - 3, 1, textureWidth - 3, 0, 3, SIZE);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, SIZE, SIZE);
                }
            }
            case ROUNDED -> {
                if (armorItems.size() > 1) {
                    int borderWidth = (SIZE - STEP) / 2;
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, SIZE - borderWidth, SIZE);
                    // nothing happens if slots <= 2
                    for (int i = 1; i < armorItems.size() - 1; i++) {
                        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, borderWidth, 1, borderWidth + i * STEP, 0, STEP, SIZE);
                    }
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 1, 1, textureWidth - STEP - borderWidth, 0, SIZE - borderWidth, SIZE);
                } else {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, SIZE, SIZE);
                }
            }
            // case NONE -> // nothing to draw ^_^
        }
        context.getMatrices().popMatrix();

        for (int i = 0; i < armorItems.size(); i++) {
            ItemStack stack = armorItems.get(i);
            int x = rect.get().getX();
            int y = rect.get().getY();

            switch (config.getOrientation()) {
                case HORIZONTAL -> x += (STEP * i);
                case VERTICAL -> y += (STEP * i);
            }

            // here I blend in slot icons if so tells the current config
            if (config.isIconsShown() && config.getWidgetShown().shouldDrawEmptySlots() && stack.isEmpty()) {
                int slotIndex = config.isReversed() ? 3 - i : i;
                Identifier identifier = PlayerScreenHandler.EMPTY_ARMOR_SLOT_TEXTURES.get(PlayerScreenHandler.EQUIPMENT_SLOT_ORDER[slotIndex]);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, x + 3, y + 3, 16, 16);
            }

            // here I draw the armour items
            this.renderHotbarItem(context, x + 3, y + 3, tickCounter, player, stack, i + 1);

            // when anchoring to the hotbar, we want the warning to be on the other side to avoid clipping with the hotbar
            Arm extrasSide = config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR ? config.getSide() : config.getSide().getOpposite();

            if (config.getAnchor().isTop() && config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                y += SIZE;
            } else if (extrasSide == Arm.RIGHT && config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
                x += SIZE;
            }

            if (config.getDurabilityDisplay() == ArmorHudConfig.DurabilityDisplay.NUMERIC) {
                String dura = String.valueOf(stack.getMaxDamage() - stack.getDamage());
                int textHeight = this.getTextRenderer().fontHeight;

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= textHeight;
                    context.drawCenteredTextWithShadow(this.getTextRenderer(), dura, x + (SIZE / 2), y, ColorHelper.fullAlpha(stack.getItemBarColor()));
                    if (config.getAnchor().isTop()) y += textHeight;
                } else {
                    int textWidth = this.getTextRenderer().getWidth(dura) + 2;
                    int textY = (SIZE - textHeight) / 2;

                    if (extrasSide == Arm.LEFT) x -= textWidth;
                    context.drawTextWithShadow(this.getTextRenderer(), dura, x + 1, y + textY, ColorHelper.fullAlpha(stack.getItemBarColor()));
                    if (extrasSide == Arm.RIGHT) x += textWidth;
                }
            }

            // here I draw warning icons if necessary
            if (config.isWarningShown() && ArmorHudMod.shouldShowWarning(stack)) {
                if (config.getWarningBobIntensity() != 0) {
                    int intensity = config.getWarningBobIntensity();
                    y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
                }

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= WARNING_SIZE + 2;

                    int warnX = (SIZE - WARNING_SIZE) / 2;
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + warnX, y + 1, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                } else {
                    if (extrasSide == Arm.LEFT) x -= WARNING_SIZE + 2;

                    int warnY = (SIZE - WARNING_SIZE) / 2;
                    context.drawTexture(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + 1, y + warnY, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                }

            }
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != Arm.RIGHT) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = ArmorHudMod.getEffectiveWidgetRect(context, player);
        if (rect.isEmpty()) return;

        shiftRef.set(rect.get().getY() + rect.get().getHeight());
    }

    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "STORE"), ordinal = 3)
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}
