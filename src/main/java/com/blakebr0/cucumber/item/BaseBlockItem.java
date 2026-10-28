package com.blakebr0.cucumber.item;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.TooltipContext;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BaseBlockItem extends ItemBlock implements TooltipContext.HoverText {
    public BaseBlockItem(Block block) {
        this(block, p -> p);
    }

    public BaseBlockItem(Block block, Function<ItemProperties, ItemProperties> properties) {
        super(block);
        properties.apply(new ItemProperties()).applyTo(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        // the block's own 1.12.2 lines, then the 1.21 ones
        super.addInformation(stack, world, tooltip, flag);
        TooltipContext.addInformation(this, stack, world, tooltip, flag);
    }

    // 1.21 tooltip lines
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<ITextComponent> tooltip, ITooltipFlag flag) { }
}
