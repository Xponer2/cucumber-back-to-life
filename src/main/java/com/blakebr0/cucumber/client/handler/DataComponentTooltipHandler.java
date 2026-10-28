package com.blakebr0.cucumber.client.handler;

import com.blakebr0.cucumber.config.ModConfigs;
import com.blakebr0.cucumber.lib.Tooltips;
import io.github.xponer2.translator.client.ItemTooltipEvent;
import io.github.xponer2.translator.component.TypedDataComponent;
import io.github.xponer2.translator.text.LiteralComponent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import io.github.xponer2.translator.component.StackComponents;

public final class DataComponentTooltipHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!ModConfigs.ENABLE_DATA_COMPONENT_TOOLTIPS.get())
            return;

        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            var stack = event.getItemStack();
            var components = StackComponents.getComponents(stack);
            var tooltip = event.getToolTip();

            if (GuiScreen.isAltKeyDown()) {
                tooltip.add(Tooltips.DATA_COMPONENTS.build());

                for (TypedDataComponent<?> component : components) {
                    tooltip.add(createTextComponent(component));
                }
            } else {
                tooltip.add(Tooltips.HOLD_ALT_FOR_DATA_COMPONENTS.build());
            }
        }
    }

    private static ITextComponent createTextComponent(TypedDataComponent<?> component) {
        var text = " " + org.apache.commons.lang3.StringUtils.abbreviate(component.toString(), 50 + 3);
        return new LiteralComponent(text).withStyle(TextFormatting.DARK_GRAY);
    }
}
