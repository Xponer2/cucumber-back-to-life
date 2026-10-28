package com.blakebr0.cucumber.client.handler;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.blakebr0.cucumber.config.ModConfigs;
import com.blakebr0.cucumber.helper.FluidHelper;
import com.blakebr0.cucumber.lib.Tooltips;
import io.github.xponer2.translator.client.ItemTooltipEvent;
import io.github.xponer2.translator.text.LiteralComponent;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import io.github.xponer2.translator.util.TagKey;
import io.github.xponer2.translator.tags.TagManager;

public final class TagTooltipHandler {
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (!ModConfigs.ENABLE_TAG_TOOLTIPS.get())
            return;

        if (Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
            var stack = event.getItemStack();
            var block = Block.getBlockFromItem(stack.getItem());

            var blockTags = block == Blocks.AIR ? Collections.emptyList() : TagManager.getTags(block.getDefaultState())
                    .map(TagKey::location)
                    .collect(Collectors.toList());
            var itemTags = TagManager.getTags(stack)
                    .map(TagKey::location)
                    .collect(Collectors.toList());

            var fluidTags = FluidHelper.getFluidTags(stack).values().stream()
                    .flatMap(List::stream)
                    .distinct()
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .collect(Collectors.toList());

            if (!blockTags.isEmpty() || !itemTags.isEmpty() || !fluidTags.isEmpty()) {
                var tooltip = event.getToolTip();

                if (GuiScreen.isCtrlKeyDown()) {
                    if (!blockTags.isEmpty()) {
                        tooltip.add(Tooltips.BLOCK_TAGS.build());

                        for (var tag : blockTags) {
                            tooltip.add(new LiteralComponent("  " + tag).withStyle(TextFormatting.DARK_GRAY));
                        }
                    }

                    if (!itemTags.isEmpty()) {
                        tooltip.add(Tooltips.ITEM_TAGS.build());

                        for (var tag : itemTags) {
                            tooltip.add(new LiteralComponent("  " + tag).withStyle(TextFormatting.DARK_GRAY));
                        }
                    }

                    if (!fluidTags.isEmpty()) {
                        tooltip.add(Tooltips.FLUID_TAGS.build());

                        for (var tag : fluidTags) {
                            tooltip.add(new LiteralComponent("  " + tag).withStyle(TextFormatting.DARK_GRAY));
                        }
                    }
                } else {
                    tooltip.add(Tooltips.HOLD_CTRL_FOR_TAGS.build());
                }
            }
        }
    }
}
