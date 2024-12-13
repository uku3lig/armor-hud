package ru.berdinskiybear.armorhud.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
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

import java.util.ArrayList;
import java.util.List;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    @Final
    private Random random;

    @Unique
    private static final int STEP = 20;
    @Unique
    private static final int WIDTH = 22;
    @Unique
    private static final int HEIGHT = 22;
    @Unique
    private static final int HOTBAR_OFFSET = 98;
    @Unique
    private static final int OFFHAND_OFFSET = 29;
    @Unique
    private static final int ATTACK_INDICATOR_OFFSET = 23;
    @Unique
    private static final int WARNING_OFFSET = 7;

    @Unique
    private static final Identifier WARNING_TEXTURE = Identifier.of("ukus-armor-hud", "warn.png");

    @Unique
    private List<ItemStack> armorItems = new ArrayList<>();
    @Unique
    private int shift = 0;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

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

        // fetch armor items
        this.armorItems = new ArrayList<>(player.getInventory().armor);
        // amount is always in [0,4], we can safely cast to int
        int nonEmptyAmount = (int) this.armorItems.stream().filter(s -> !s.isEmpty()).count();

        // return if there is nothing to draw
        if (nonEmptyAmount == 0 && config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        if (config.isReversed())  {
            armorItems = armorItems.reversed();
        }

        // push them matrices :3
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 200);

        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final int sideMultiplier, sideOffsetMultiplier;
        if ((config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.LEFT)
                || (config.getAnchor() != ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.RIGHT)) {
            sideMultiplier = -1;
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1;
            sideOffsetMultiplier = 0;
        }

        final int verticalMultiplier = switch (config.getAnchor()) {
            case TOP, TOP_CENTER -> 1;
            case BOTTOM, HOTBAR -> -1;
        };

        final int verticalOffsetMultiplier = switch (config.getAnchor()) {
            case TOP, TOP_CENTER -> 0;
            case BOTTOM, HOTBAR -> -1;
        };

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
            case ALWAYS_LEAVE_SPACE -> Math.max(OFFHAND_OFFSET, ATTACK_INDICATOR_OFFSET);
            case ADHERE -> {
                if (player.getMainArm().getOpposite() == config.getSide().asArm()) {
                    if (!player.getOffHandStack().isEmpty()) {
                        yield OFFHAND_OFFSET;
                    } else if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                        yield ATTACK_INDICATOR_OFFSET;
                    }
                }

                yield 0;
            }
        };

        final int slots = config.getWidgetShown() == ArmorHudConfig.WidgetShown.NOT_EMPTY ? nonEmptyAmount : 4;
        final int widgetWidth = WIDTH + ((slots - 1) * STEP);

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> context.getScaledWindowWidth() / 2 - (widgetWidth / 2);
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR ->
                    context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
        };

        final int armorWidgetY = config.getOffsetY() * verticalMultiplier + switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> context.getScaledWindowHeight() - HEIGHT;
            case TOP, TOP_CENTER -> 0;
        };

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // here I draw the slots
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, -91);
        switch (config.getStyle()) {
            case HOTBAR -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, widgetWidth - 3, HEIGHT);
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, HEIGHT);
            }
            case ROUNDED_CORNERS -> {
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, 3, HEIGHT);
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_TEXTURE, 182, 22, 3, 0, armorWidgetX + 3, armorWidgetY, widgetWidth - 6, HEIGHT);
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, WIDTH - 3, 1, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, HEIGHT);
            }
            case ROUNDED -> {
                int borderWidth = (WIDTH - STEP) / 2;
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, borderWidth, HEIGHT);
                for (int i = 0; i < slots; i++) {
                    context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, borderWidth, 1, armorWidgetX + borderWidth + i * STEP, armorWidgetY, STEP, HEIGHT);
                }
                context.drawGuiTexture(RenderLayer::getGuiTextured, InGameHud.HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX + widgetWidth - borderWidth, armorWidgetY, borderWidth, HEIGHT);
            }
        }
        context.getMatrices().pop();

        // here I draw warning icons if necessary
        if (config.isWarningShown()) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, 90);

            int i = 0;
            for (ItemStack stack : armorItems) {
                if (ArmorHudMod.shouldShowWarning(stack)) {
                    int x = armorWidgetX + (STEP * i) + WARNING_OFFSET;
                    int y = armorWidgetY + (HEIGHT * (verticalOffsetMultiplier + 1)) + (8 * verticalOffsetMultiplier);

                    if (config.getWarningBobIntensity() != 0) {
                        int intensity = config.getWarningBobIntensity();
                        y += (int) (this.random.nextInt(intensity) - Math.ceil(intensity / 2F));
                    }

                    context.drawTexture(RenderLayer::getGuiTextured, WARNING_TEXTURE, x, y, 0, 0, 8, 8, 8, 8);
                    i++;
                } else if (config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY || !stack.isEmpty()) {
                    i++;
                }
            }

            context.getMatrices().pop();
        }

        // here I blend in slot icons if so tells the current config
        if (config.isIconsShown() && config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY) {
            context.getMatrices().push();
            context.getMatrices().translate(0, 0, -90);
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

            for (int i = 0; i < armorItems.size(); i++) {
                if (armorItems.get(i).isEmpty()) {
                    int slotIndex = config.isReversed() ? i : 3 - i;
                    Identifier texture = PlayerScreenHandler.EMPTY_ARMOR_SLOT_TEXTURES.get(PlayerScreenHandler.EQUIPMENT_SLOT_ORDER[slotIndex]);
                    context.drawGuiTexture(RenderLayer::getGuiTextured, texture, armorWidgetX + (STEP * i) + 3, armorWidgetY + 3, 16, 16);
                }
            }

            RenderSystem.defaultBlendFunc();
            context.getMatrices().pop();
        }

        // and at last I draw the armour items
        int i = 0;
        for (ItemStack stack : armorItems) {
            if (!stack.isEmpty()) {
                this.renderHotbarItem(context, armorWidgetX + (STEP * i) + 3, armorWidgetY + 3, tickCounter, player, stack, i + 1);
            }

            if (!stack.isEmpty() || config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY) {
                i++;
            }
        }

        // remove my translations
        context.getMatrices().pop();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.BY, by = 2))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        int amount = (int) player.getInventory().armor.stream().filter(s -> !s.isEmpty()).count();
        if (amount == 0 || config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        int newShift = 22 + config.getOffsetY();
        if (config.isWarningShown() && this.armorItems.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
            newShift += 10;
            if (config.getWarningBobIntensity() != 0) {
                newShift += 7;
            }
        }

        this.shift = Math.max(newShift, 0);
    }

    @ModifyVariable(method = "renderStatusEffectOverlay", at = @At(value = "STORE"), ordinal = 3)
    public int statusEffectIconsOffset(int y) {
        return y + this.shift;
    }
}
