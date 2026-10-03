package ru.berdinskiybear.armorhud;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import ru.berdinskiybear.armorhud.config.ArmorHudConfig;
import ru.berdinskiybear.armorhud.mixin.HudAccessor;
import ru.berdinskiybear.armorhud.mixin.InventoryMenuAccessor;

import java.util.List;
import java.util.Optional;

import static ru.berdinskiybear.armorhud.ArmorHudMod.*;

public class ArmorHudRenderer {
    private static final Identifier HOTBAR_SPRITE = HudAccessor.getHOTBAR_SPRITE();
    private static final Identifier HOTBAR_OFFHAND_LEFT_SPRITE = HudAccessor.getHOTBAR_OFFHAND_LEFT_SPRITE();

    public static final Identifier WARNING_TEXTURE = Identifier.fromNamespaceAndPath(MOD_ID, "warn.png");

    public static void drawArmorHud(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        ArmorHudConfig config = ArmorHudMod.getManager().getConfig();
        if (!config.isEnabled()) return;

        Player player = ArmorHudMod.getCameraPlayer();
        if (player == null) return;

        final Optional<Rect2i> rect = ArmorHudMod.getWidgetRect(graphics, player);
        // return if there is nothing to draw
        if (rect.isEmpty()) return;

        // fetch armor items
        List<ItemStack> armorItems = ArmorHudMod.getArmorItems(player);
        if (config.isReversed()) {
            armorItems = armorItems.reversed();
        }

        final int textureWidth = SIZE + ((armorItems.size() - 1) * STEP);

        // here I draw the slots
        graphics.pose().pushMatrix();
        graphics.pose().translate(rect.get().getX(), rect.get().getY());

        if (config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
            graphics.pose().rotate(Mth.HALF_PI).translate(0, -SIZE);
        }

        int color = ARGB.white(ArmorHudMod.getModCompat().hudOpacity());
        HudAccessor hud = (HudAccessor) Minecraft.getInstance().gui.hud;

        switch (config.getStyle()) {
            case HOTBAR -> {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 0, 0, 0, 0, textureWidth - 3, SIZE, color);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 182 - 3, 0, textureWidth - 3, 0, 3, SIZE, color);
            }
            case ROUNDED_CORNERS -> {
                if (armorItems.size() > 1) {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, 3, SIZE, color);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, 182, 22, 3, 0, 3, 0, textureWidth - 6, SIZE, color);
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, SIZE - 3, 1, textureWidth - 3, 0, 3, SIZE, color);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                }
            }
            case ROUNDED -> {
                if (armorItems.size() > 1) {
                    int borderWidth = (SIZE - STEP) / 2;
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE - borderWidth, SIZE, color);
                    // nothing happens if slots <= 2
                    for (int i = 1; i < armorItems.size() - 1; i++) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, borderWidth, 1, borderWidth + i * STEP, 0, STEP, SIZE, color);
                    }
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 1, 1, textureWidth - STEP - borderWidth, 0, SIZE - borderWidth, SIZE, color);
                } else {
                    graphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, 29, 24, 0, 1, 0, 0, SIZE, SIZE, color);
                }
            }
            // case NONE -> // nothing to draw ^_^
        }
        graphics.pose().popMatrix();

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
                Identifier identifier = InventoryMenuAccessor.getTEXTURE_EMPTY_SLOTS().get(SLOT_IDS[slotIndex]);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, x + 3, y + 3, 16, 16);
            }

            // here I draw the armour items
            hud.armorhud$extractSlot(graphics, x + 3, y + 3, tickCounter, player, stack, i + 1);

            // when anchoring to the hotbar, we want the warning to be on the other side to avoid clipping with the hotbar
            ArmorHudConfig.Side extrasSide = config.getAnchor() == ArmorHudConfig.Anchor.HOTBAR ? config.getSide() : config.getSide().getOpposite();

            if (config.getAnchor().isTop() && config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                y += SIZE;
            } else if (extrasSide == ArmorHudConfig.Side.RIGHT && config.getOrientation() == ArmorHudConfig.Orientation.VERTICAL) {
                x += SIZE;
            }

            if (config.getDurabilityDisplay() != ArmorHudConfig.DurabilityDisplay.BAR && !stack.isEmpty() && stack.isDamageableItem()) {
                Font font = Minecraft.getInstance().font;
                String dura = ArmorHudMod.getDurabilityText(stack);
                int textHeight = font.lineHeight;

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= textHeight;
                    graphics.centeredText(font, dura, x + (SIZE / 2), y, ARGB.opaque(stack.getBarColor()));
                    if (config.getAnchor().isTop()) y += textHeight;
                } else {
                    int textWidth = font.width(dura) + 2;
                    int textY = (SIZE - textHeight) / 2;

                    if (extrasSide == ArmorHudConfig.Side.LEFT) x -= textWidth;
                    graphics.text(font, dura, x + 1, y + textY, ARGB.opaque(stack.getBarColor()));
                    if (extrasSide == ArmorHudConfig.Side.RIGHT) x += textWidth;
                }
            }

            // here I draw warning icons if necessary
            if (config.isWarningShown() && ArmorHudMod.shouldShowWarning(stack)) {
                if (config.getWarningBobIntensity() != 0) {
                    int intensity = config.getWarningBobIntensity();
                    y += (int) (hud.getRandom().nextInt(intensity) - Math.ceil(intensity / 2F));
                }

                if (config.getOrientation() == ArmorHudConfig.Orientation.HORIZONTAL) {
                    if (!config.getAnchor().isTop()) y -= WARNING_SIZE + 2;

                    int warnX = (SIZE - WARNING_SIZE) / 2;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + warnX, y + 1, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                } else {
                    if (extrasSide == ArmorHudConfig.Side.LEFT) x -= WARNING_SIZE + 2;

                    int warnY = (SIZE - WARNING_SIZE) / 2;
                    graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + 1, y + warnY, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
                }
            }
        }
    }

    // this part is separate from the rest so that the text can properly render above all the gui elements
    // plus this allows for main/offhand things to be rendered even if the armor widget is empty
    public static void renderMainAndOffhand(GuiGraphicsExtractor graphics) {
        ArmorHudConfig config = getManager().getConfig();
        if (!config.isEnabled()) return;

        Player player = getCameraPlayer();
        if (player == null) return;

        Font font = Minecraft.getInstance().font;
        HudAccessor hud = (HudAccessor) Minecraft.getInstance().gui.hud;

        ItemStack offhand = player.getOffhandItem();
        if (offhand.isDamageableItem() && config.isOffHandDurability()) {
            // offhand slot is offset 7 pixels from the hotbar
            int x = player.getMainArm() == HumanoidArm.RIGHT
                    ? (graphics.guiWidth() / 2) - 91 - 29
                    : (graphics.guiWidth() / 2) + 91 + 7;
            int y = graphics.guiHeight() - SIZE - font.lineHeight;

            if (config.getDurabilityDisplay() != ArmorHudConfig.DurabilityDisplay.BAR) {
                String dura = ArmorHudMod.getDurabilityText(offhand);
                graphics.centeredText(font, dura, x + (SIZE / 2), y, ARGB.opaque(offhand.getBarColor()));
            }

            if (config.isWarningShown() && shouldShowWarning(offhand)) {
                if (config.getWarningBobIntensity() != 0) {
                    int intensity = config.getWarningBobIntensity();
                    y += (int) (hud.getRandom().nextInt(intensity) - Math.ceil(intensity / 2F));
                }

                y -= WARNING_SIZE + 2;
                int warnX = (SIZE - WARNING_SIZE) / 2;
                graphics.blit(RenderPipelines.GUI_TEXTURED, WARNING_TEXTURE, x + warnX, y + 1, 0, 0, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE, WARNING_SIZE);
            }
        }

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isDamageableItem() && config.isMainHandDurability() && config.getDurabilityDisplay() != ArmorHudConfig.DurabilityDisplay.BAR) {
            // same as offhand but on the other side
            int x = (graphics.guiWidth() / 2) - 91 + player.getInventory().getSelectedSlot() * 20 + (SIZE / 2);
            int y = graphics.guiHeight() - SIZE - 1;

            String dura = ArmorHudMod.getDurabilityText(mainHand);
            int textX = x - (font.width(dura) / 2);
            // draw text with an outline for better visibility
            graphics.text(font, dura, textX + 1, y, ARGB.black(255), false);
            graphics.text(font, dura, textX - 1, y, ARGB.black(255), false);
            graphics.text(font, dura, textX, y + 1, ARGB.black(255), false);
            graphics.text(font, dura, textX, y - 1, ARGB.black(255), false);
            graphics.text(font, dura, textX, y, ARGB.opaque(mainHand.getBarColor()), false);
        }
    }
}
