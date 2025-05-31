package ru.berdinskiybear.armorhud.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.berdinskiybear.armorhud.ArmorHudMod;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;

import java.util.List;

import static ru.berdinskiybear.armorhud.mixin.Constants.*;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Shadow
    @Final
    private static Identifier HOTBAR_TEXTURE;
    @Shadow
    @Final
    private static Identifier HOTBAR_OFFHAND_LEFT_TEXTURE;

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int ticks;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Inject(method = "renderHotbarVanilla", at = @At("TAIL"))
    public void renderArmorHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled()) return;

        PlayerEntity player = getCameraPlayer();
        if (player == null) return;

        // fetch armor items
        List<ItemStack> armor = player.getInventory().armor;
        final int nonEmptyAmount = (int) armor.stream().filter(s -> !s.isEmpty()).count();

        // return if there is nothing to draw
        if (armor.isEmpty() && config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        if (config.isReversed())
            armor = armor.reversed();

        // push them matrices :3
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(0, 0, 200);


        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final ArmorHudConfig.Anchor anchor = config.getAnchor();
        final boolean anchorTop = anchor.isTop();
        final int sideMultiplier, sideOffsetMultiplier;
        if ((anchor == ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.LEFT)
                || (anchor != ArmorHudConfig.Anchor.HOTBAR && config.getSide() == ArmorHudConfig.Side.RIGHT)) {
            sideMultiplier = -1;
            sideOffsetMultiplier = -1;
        } else {
            sideMultiplier = 1;
            sideOffsetMultiplier = 0;
        }

        final boolean showEmpty = config.getWidgetShown() != ArmorHudConfig.WidgetShown.NOT_EMPTY;
        final int slots = showEmpty ? armor.size() : nonEmptyAmount;
        final int widgetWidth = SIZE + ((slots - 1) * STEP);

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (anchor) {
            case TOP_CENTER -> context.getScaledWindowWidth() / 2 - (widgetWidth / 2);
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR -> {
                final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
                    case ALWAYS_IGNORE -> 0;
                    case ALWAYS_LEAVE_SPACE -> OFFHAND_OFFSET;
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
                yield context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
            }
        };

        final int armorWidgetY = anchorTop ? config.getOffsetY() : -config.getOffsetY() + context.getScaledWindowHeight() - SIZE;
        final int yPlus3 = armorWidgetY + 3;

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // here I draw the slots
        matrices.push();
        matrices.translate(0, 0, -91);
        switch (config.getStyle()) {
            case HOTBAR -> {
                context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 0, 0, armorWidgetX, armorWidgetY, widgetWidth - 3, SIZE);
                context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 182 - 3, 0, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, SIZE);
            }
            case ROUNDED_CORNERS -> {
                context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, 3, SIZE);
                context.drawGuiTexture(HOTBAR_TEXTURE, 182, 22, 3, 0, armorWidgetX + 3, armorWidgetY, widgetWidth - 6, SIZE);
                context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, SIZE - 3, 1, armorWidgetX + widgetWidth - 3, armorWidgetY, 3, SIZE);
            }
            case ROUNDED -> {
                int borderWidth = (SIZE - STEP) / 2;
                context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX, armorWidgetY, borderWidth, SIZE);
                for (int i = 0; i < slots; i++)
                    context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, borderWidth, 1, armorWidgetX + borderWidth + i * STEP, armorWidgetY, STEP, SIZE);
                context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, 29, 24, 0, 1, armorWidgetX + widgetWidth - borderWidth, armorWidgetY, borderWidth, SIZE);
            }
        }
        matrices.pop();

        // here I blend in slot icons if so tells the current config
        if (config.isIconsShown() && showEmpty) {
            matrices.push();
            matrices.translate(0, 0, -90);
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_COLOR, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);

            SpriteAtlasTexture atlas = this.client.getBakedModelManager().getAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
            for (int i = 0, size = armor.size() - 1; i <= size; i++) {
                if (armor.get(i).isEmpty()) {
                    int slotIndex = config.isReversed() ? i : size - i;
                    Identifier spriteId = PlayerScreenHandlerAccessor.getEMPTY_ARMOR_SLOT_TEXTURES().get(PlayerScreenHandlerAccessor.getEQUIPMENT_SLOT_ORDER()[slotIndex]);
                    Sprite sprite = atlas.getSprite(spriteId);

                    context.drawSprite(armorWidgetX + (STEP * i) + 3, yPlus3, 0, 16, 16, sprite);
                }
            }

            RenderSystem.defaultBlendFunc();
            matrices.pop();
        }

        // draw the armour items and the warning signs if necessary
        final boolean showWarning = config.isWarningShown();
        int widgetX = armorWidgetX;
        int warningY = anchorTop ? armorWidgetY + SIZE : armorWidgetY - WARNING_OFFSET;
        final int intensity = config.getWarningBobIntensity();
        if (intensity != 0) {
            int bob = (int) Math.round(Math.sin((ticks / 2.0) % Math.TAU) / 2.0 * intensity); // hi bob
            warningY += bob;
        }
        for (ItemStack stack : armor) {
            if (showWarning) {
                if (ArmorHudMod.shouldShowWarning(stack)) {
                    int x = widgetX + WARNING_OFFSET;
                    context.drawTexture(ArmorHudMod.WARNING_TEXTURE, x, warningY, 0, 0, 0, 8, 8, 8, 8);
                }
            }

            if (!stack.isEmpty()) {
                this.renderHotbarItem(context, widgetX + 3, yPlus3, tickCounter, player, stack, widgetX);
                widgetX += STEP;
            } else if (showEmpty) {
                widgetX += STEP;
            }
        }

        // remove my translations
        matrices.pop();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci, @Share("shift") LocalIntRef shiftRef) {
        ArmorHudConfig config = ArmorHudConfig.CONFIG;
        if (config.isDisabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != ArmorHudConfig.Side.RIGHT) return;

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        List<ItemStack> armor = ArmorHudMod.nonEmptyArmor(player);
        if (armor.isEmpty() || config.getWidgetShown() != ArmorHudConfig.WidgetShown.ALWAYS) return;

        int newShift = SIZE + config.getOffsetY();
        if (config.isWarningShown() && armor.stream().anyMatch(ArmorHudMod::shouldShowWarning)) {
            newShift += 10;
            if (config.getWarningBobIntensity() != 0) {
                newShift += WARNING_OFFSET;
            }
        }

        shiftRef.set(Math.max(newShift, 0));
    }

    @ModifyExpressionValue(method = "renderStatusEffectOverlay", at = @At(value = "CONSTANT", args = "intValue=1"))
    public int statusEffectIconsOffset(int y, @Share("shift") LocalIntRef shiftRef) {
        return y + shiftRef.get();
    }
}