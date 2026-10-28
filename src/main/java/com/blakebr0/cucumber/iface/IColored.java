package com.blakebr0.cucumber.iface;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface IColored {
	default int getColor(int index) {
		return -1;
	}

	default int getColor(int index, ItemStack stack) {
		return this.getColor(index);
	}

	class BlockColors implements IBlockColor {
		@Override
		public int colorMultiplier(IBlockState state, IBlockAccess level, BlockPos pos, int index) {
			return ((IColored) state.getBlock()).getColor(index);
		}
	}

	class ItemColors implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int index) {
			return ((IColored) stack.getItem()).getColor(index, stack);
		}
	}

	class ItemBlockColors implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int index) {
			return ((IColored) Block.getBlockFromItem(stack.getItem())).getColor(index, stack);
		}
	}
}
