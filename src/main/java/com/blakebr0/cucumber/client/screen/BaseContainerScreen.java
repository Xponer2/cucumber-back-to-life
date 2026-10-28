package com.blakebr0.cucumber.client.screen;

import java.text.NumberFormat;

import com.blakebr0.cucumber.util.Localizable;
import io.github.xponer2.translator.client.AbstractContainerScreen;
import io.github.xponer2.translator.menu.AbstractContainerMenu;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import io.github.xponer2.translator.client.GuiGraphics;

public abstract class BaseContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected ResourceLocation bgTexture;
    protected int bgImgWidth;
    protected int bgImgHeight;

    public BaseContainerScreen(T container, InventoryPlayer inventory, ITextComponent title, ResourceLocation bgTexture, int bgWidth, int bgHeight) {
        this(container, inventory, title, bgTexture, bgWidth, bgHeight, 256, 256);
    }

    public BaseContainerScreen(T container, InventoryPlayer inventory, ITextComponent title, ResourceLocation bgTexture, int bgWidth, int bgHeight, int bgImgWidth, int bgImgHeight) {
        super(container, inventory, title);
        this.xSize = bgWidth;
        this.ySize = bgHeight;
        this.bgTexture = bgTexture;
        this.bgImgWidth = bgImgWidth;
        this.bgImgHeight = bgImgHeight;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gfx, mouseX, mouseY, partialTicks);
        super.render(gfx, mouseX, mouseY, partialTicks);
        this.renderTooltip(gfx, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        int x = this.getGuiLeft();
        int y = this.getGuiTop();

        gfx.blit(this.bgTexture, x, y, 0, 0, this.xSize, this.ySize, this.bgImgWidth, this.bgImgHeight);
    }

    protected static String text(String key, Object... args) {
        return Localizable.of(key).args(args).buildString();
    }

    protected static String number(Object number) {
        return NumberFormat.getInstance().format(number);
    }
}
