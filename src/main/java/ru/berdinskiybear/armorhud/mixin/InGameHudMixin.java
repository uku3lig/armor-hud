package ru.berdinskiybear.armorhud.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
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

import java.util.ArrayList;
import java.util.List;

import static ru.berdinskiybear.armorhud.ArmorHudMod.ARMOR_SLOTS;

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
        this.armorItems = ArmorHudMod.getArmorItems(player);
        // return if there is nothing to draw
        if (this.armorItems.isEmpty()) return;

        if (config.isReversed()) {
            armorItems = armorItems.reversed();
        }

        context.getMatrices().pushMatrix();

        // hotbar offset is relative to the bar, so when we are on the left it needs to be flipped
        // and on the right side, we need to flip the offset, except when anchored to the hotbar
        final int sideMultiplier, sideOffsetMultiplier;
        if ((config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR && config.getSide() == Arm.LEFT)
                || (config.getAnchor() != ArmorHudConfig.Anchor.HOTBAR && config.getSide() == Arm.RIGHT)) {
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

        final int addedHotbarOffset = switch (config.getOffhandSlotBehavior()) {
            case ALWAYS_IGNORE -> 0;
            case ALWAYS_LEAVE_SPACE -> Math.max(OFFHAND_OFFSET, ATTACK_INDICATOR_OFFSET);
            case ADHERE -> {
                if (player.getMainArm().getOpposite() == config.getSide()) {
                    if (!player.getOffHandStack().isEmpty()) {
                        yield OFFHAND_OFFSET;
                    } else if (this.client.options.getAttackIndicator().getValue() == AttackIndicator.HOTBAR) {
                        yield ATTACK_INDICATOR_OFFSET;
                    }
                }

                yield 0;
            }
        };

        final int textureWidth = WIDTH + ((this.armorItems.size() - 1) * STEP);

        final int widgetWidth = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? WIDTH : textureWidth;
        final int widgetHeight = config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL ? textureWidth : HEIGHT;

        final int armorWidgetX = config.getOffsetX() * sideMultiplier + switch (config.getAnchor()) {
            case TOP_CENTER -> context.getScaledWindowWidth() / 2 - (widgetWidth / 2);
            case TOP, BOTTOM -> (widgetWidth - context.getScaledWindowWidth()) * sideOffsetMultiplier;
            case HOTBAR ->
                    context.getScaledWindowWidth() / 2 + ((HOTBAR_OFFSET + addedHotbarOffset) * sideMultiplier) + (widgetWidth * sideOffsetMultiplier);
        };

        final int armorWidgetY = config.getOffsetY() * verticalMultiplier + switch (config.getAnchor()) {
            case BOTTOM, HOTBAR -> context.getScaledWindowHeight() - widgetHeight;
            case TOP, TOP_CENTER -> 0;
        };

        // here I draw the slots
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(armorWidgetX, armorWidgetY);

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
                for (int i = 0; i < this.armorItems.size(); i++) {
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
            int slotX = armorWidgetX;
            int slotY = armorWidgetY;

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
                        final int verticalOffsetMultiplier = switch (config.getAnchor()) {
                            case TOP, TOP_CENTER -> 0;
                            case BOTTOM, HOTBAR -> -1;
                        };

                        x += WARNING_OFFSET;
                        y += (HEIGHT * (verticalOffsetMultiplier + 1)) + (8 * verticalOffsetMultiplier);
                    }
                    case VERTICAL -> {
                        // when anchoring to the hotbar, we want the warning to be on the other side to avoid clipping with the hotbar
                        Arm warningSide = config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR ? config.getSide().getOpposite() : config.getSide();
                        final int horizontalOffsetMultiplier = switch (warningSide) {
                            case LEFT -> 0;
                            case RIGHT -> -1;
                        };

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

        // remove my translations
        context.getMatrices().popMatrix();
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.BY, by = 2))
    public void calculateStatusEffectIconsOffset(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled() || !config.isPushStatusEffectIcons() || config.getAnchor() != ArmorHudConfig.Anchor.TOP
                || config.getSide() != Arm.RIGHT) return;

        PlayerEntity player = this.getCameraPlayer();
        if (player == null) return;

        int amount = (int) ARMOR_SLOTS.stream().map(i -> player.getInventory().getStack(i)).filter(s -> !s.isEmpty()).count();
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
