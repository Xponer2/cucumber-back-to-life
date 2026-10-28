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
import net.minecraftforge.energy.IEnergyStorage;

import io.github.xponer2.translator.client.GuiGraphics;

public class EnergyBarWidget extends AbstractWidget {
    private static final ResourceLocation WIDGETS_TEXTURE = Cucumber.resource("textures/gui/widgets.png");

    private final IntSupplier energy;
    private final IntSupplier capacity;

    @Deprecated
    public EnergyBarWidget(int x, int y, IEnergyStorage energy) {
        this(x, y, energy::getEnergyStored, energy::getMaxEnergyStored);
    }

    public EnergyBarWidget(int x, int y, IntSupplier energy, IntSupplier capacity) {
        super(x, y, 14, 78, new LiteralComponent("Energy Bar"));
        this.energy = energy;
        this.capacity = capacity;
        this.active = false; // not a clickable element
    }

    @Override
    public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
        int offset = this.getEnergyBarOffset();

        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY(), 0, 0, this.width, this.height);
        gfx.blit(WIDGETS_TEXTURE, this.getX(), this.getY() + this.height - offset, 14, this.height - offset, this.width,  offset + 1);

        if (mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height) {
            var font = Minecraft.getMinecraft().fontRenderer;
            var text = Formatting.number(this.energy.getAsInt()).append(" / ").append(Formatting.energy(this.capacity.getAsInt()));

            gfx.renderTooltip(font, text, mouseX, mouseY);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) { }

    private int getEnergyBarOffset() {
        int i = this.energy.getAsInt();
        int j = this.capacity.getAsInt();
        return (int) (j != 0 && i != 0 ? i * (long) this.height / j : 0);
    }
}
