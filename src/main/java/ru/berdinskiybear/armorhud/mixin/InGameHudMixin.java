package ru.berdinskiybear.armorhud.mixin;

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

    @Unique
    private int shift = 0;

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

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

        final Optional<Rect2i> rect = getWidgetRect(context, player);
        // return if there is nothing to draw
        if (rect.isEmpty()) return;

        // fetch armor items
        List<ItemStack> armorItems = ArmorHudMod.getArmorItems(player);
        if (config.isReversed()) {
            armorItems = armorItems.reversed();
        }

        final int textureWidth = WIDTH + ((armorItems.size() - 1) * STEP);

        // here I draw the slots
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(rect.get().getX(), rect.get().getY());

        if (config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
            context.getMatrices().rotate(org.joml.Math.toRadians(90f)).translate(0, -22);
        }

        switch (config.getStyle()) {
            case HOTBAR -> {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 0, 0, 0, 0, textureWidth - 3, HEIGHT);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, HEIGHT);
            }
            case ROUNDED_CORNERS -> {
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, 3, HEIGHT);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_TEXTURE, 182, 22, 3, 0, 3, 0, textureWidth - 6, HEIGHT);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, WIDTH - 3, 1, textureWidth - 3, 0, 3, HEIGHT);
            }
            case ROUNDED -> {
                int borderWidth = (WIDTH - STEP) / 2;
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, 0, 0, borderWidth, HEIGHT);
                for (int i = 0; i < armorItems.size(); i++) {
                    context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, borderWidth, 1, borderWidth + i * STEP, 0, STEP, HEIGHT);
                }
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, textureWidth - borderWidth, 0, borderWidth, HEIGHT);
            }
            case NONE -> {
                // nothing to draw ^_^
            }
        }
        context.getMatrices().popMatrix();

        for (int i = 0; i < armorItems.size(); i++) {
            ItemStack stack = armorItems.get(i);
            int slotX = rect.get().getX();
            int slotY = rect.get().getY();

            switch (config.getOrientation()) {
                case HORIZONTAL -> slotX += (STEP * i);
                case VERTICAL -> slotY += (STEP * i);
            }

            // here I blend in slot icons if so tells the current config
            if (config.isIconsShown() && config.getWidgetShown().shouldDrawEmptySlots() && stack.isEmpty()) {
                int slotIndex = config.isReversed() ? 3 - i : i;
                Identifier identifier = PlayerScreenHandler.EMPTY_ARMOR_SLOT_TEXTURES.get(PlayerScreenHandler.EQUIPMENT_SLOT_ORDER[slotIndex]);
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, identifier, slotX + 3, slotY + 3, 16, 16);
            }

            // here I draw the armour items
            this.renderHotbarItem(context, slotX + 3, slotY + 3, tickCounter, player, stack, i + 1);

            // here I draw warning icons if necessary
            if (config.isWarningShown() && ArmorHudMod.shouldShowWarning(stack)) {
                int x = slotX;
                int y = slotY;

                switch (config.getOrientation()) {
                    case HORIZONTAL -> {
                        final int verticalOffsetMultiplier = config.getAnchor().isTop() ? 0 : -1;

                        x += WARNING_OFFSET;
                        y += (HEIGHT * (verticalOffsetMultiplier + 1)) + (8 * verticalOffsetMultiplier);
                    }
                    case VERTICAL -> {
                        // when anchoring to the hotbar, we want the warning to be on the other side to avoid clipping with the hotbar
                        Arm warningSide = config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR ? config.getSide().getOpposite() : config.getSide();
                        final int horizontalOffsetMultiplier = warningSide == Arm.LEFT ? 0 : -1;

                        x += (WIDTH * (horizontalOffsetMultiplier + 1)) + (8 * horizontalOffsetMultiplier);
                        y += WARNING_OFFSET;
                    }
                }

                if (config.getWarningBobIntensity() != 0) {
                    int intensity = config.getWarningBobIntensity();
                    y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
                }

                context.drawTexture(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);
            }
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        this.shift = 0;
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != Arm.RIGHT) return;

        PlayerEntity player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        Optional<Rect2i> rect = ArmorHudMod.getEffectiveWidgetRect(context, player);
        if (rect.isEmpty()) return;

        this.shift = rect.get().getY() + rect.get().getHeight();
    }

    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "STORE"), ordinal = 3)
    public int statusEffectIconsOffset(int y) {
        return y + this.shift;
    }
}
