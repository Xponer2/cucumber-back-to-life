package com.blakebr0.cucumber.client.screen.widget;

import java.util.function.IntSupplier;

import com.blakebr0.cucumber.Cucumber;
import com.blakebr0.cucumber.util.Formatting;
import io.github.xponer2.translator.client.AbstractWidget;
import io.github.xponer2.translator.client.NarrationElementOutput;
import io.github.xponer2.translator.text.LiteralComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import io.github.xponer2.translator.client.GuiGraphics;

public class FuelWidget extends AbstractWidget {
    private static final ResourceLocation WIDGETS_TEXTURE = Cucumber.resource("textures/gui/widgets.png");

    private final IntSupplier fuelValue;
    private final IntSupplier fuelLeft;

    public FuelWidget(int x, int y, IntSupplier fuelValue, IntSupplier fuelLeft) {
        super(x, y, 14, 14, new LiteralComponent("Fuel"));
        this.fuelValue = fuelValue;
        this.fuelLeft = fuelLeft;
        this.active = false; // not a clickable element
    }

    @Override
    protected void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int offset = this.getBurnLeftScaled();

        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), 30, 0, this.width, this.height);
        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY() + this.height - offset, 44, this.height - offset, this.width,  offset + 1);

        if (mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height) {
            var font = Minecraft.getMinecraft().fontRenderer;
            var text = Formatting.energy(this.fuelLeft.getAsInt());

            gfx.renderTooltip(font, text, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) { }

    private int getBurnLeftScaled() {
        int i = this.fuelLeft.getAsInt();
        int j = this.fuelValue.getAsInt();
        return (int) (j != 0 && i != 0 ? (long) i * this.height / j : 0);
    }
}
