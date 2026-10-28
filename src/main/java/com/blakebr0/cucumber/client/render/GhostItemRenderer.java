package com.blakebr0.cucumber.client.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

public final class GhostItemRenderer {
    // 1.21 draws the item with a translucent render type; 1.12.2 item quads ignore the GL color, so the item is
    // drawn normally and covered with the slot color at half alpha
    private static final int GHOST_OVERLAY = 0x808B8B8B;

    public static void renderItemIntoGui(ItemStack stack, int x, int y, RenderItem itemRenderer) {
        if (stack.isEmpty()) {
            return;
        }

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        itemRenderer.renderItemAndEffectIntoGUI(stack, x, y);
        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Gui.drawRect(x, y, x + 16, y + 16, GHOST_OVERLAY);
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }
}
