package com.blakebr0.cucumber.client.screen.widget;

import java.util.function.IntSupplier;

import com.blakebr0.cucumber.Cucumber;
import io.github.xponer2.translator.client.AbstractWidget;
import io.github.xponer2.translator.client.NarrationElementOutput;
import io.github.xponer2.translator.text.LiteralComponent;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import io.github.xponer2.translator.client.GuiGraphics;

public class ProgressArrowWidget extends AbstractWidget {
    private static final ResourceLocation WIDGETS_TEXTURE = Cucumber.resource("textures/gui/widgets.png");

    private final IntSupplier progress;
    private final IntSupplier total;

    public ProgressArrowWidget(int x, int y, IntSupplier progress, IntSupplier total) {
        super(x, y, 24, 16, new LiteralComponent("Progress Arrow"));
        this.progress = progress;
        this.total = total;
        this.active = false; // not a clickable element
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int offset = this.getProgressScaled();

        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), 30, 16, this.width, this.height);
        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), 30, 16 + this.height, offset + 1, this.height);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    private int getProgressScaled() {
        int i = this.progress.getAsInt();
        int j = this.total.getAsInt();
        return j != 0 && i != 0 ? i * this.width / j : 0;
    }
}
