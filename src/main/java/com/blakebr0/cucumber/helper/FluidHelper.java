package com.blakebr0.cucumber.helper;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

public final class FluidHelper {
	// 1.12.2: no fluid stack means empty (FluidStack.EMPTY in 1.21)
	@Nullable
	public static FluidStack getFluidFromStack(ItemStack stack) {
		return FluidUtil.getFluidContained(stack);
	}

	public static int getFluidAmount(ItemStack stack) {
		var fluid = getFluidFromStack(stack);
		return fluid == null ? 0 : fluid.amount;
	}

	public static ItemStack getFilledBucket(FluidStack fluid, Item bucket, int capacity) {
		if (fluid != null && FluidRegistry.isFluidRegistered(fluid.getFluid())) {
			var filledBucket = new ItemStack(bucket);
			var fluidContents = new FluidStack(fluid, capacity);

			var tank = FluidUtil.getFluidHandler(filledBucket);
			if (tank != null) {
				tank.fill(fluidContents, true);
				return tank.getContainer();
			}

			return filledBucket;
		}

		return ItemStack.EMPTY;
	}

	public static int toBuckets(int i) {
		return i - (i % 1000);
	}

	// 1.12.2 has no fluid tags: the fluids of the stack, each without tags
	public static Map<Fluid, List<ResourceLocation>> getFluidTags(ItemStack stack) {
		var fluidHandler = FluidUtil.getFluidHandler(stack);
		if (fluidHandler == null) {
			return Collections.emptyMap();
		}

		var fluidTagsMap = new LinkedHashMap<Fluid, List<ResourceLocation>>();
		for (var tank : fluidHandler.getTankProperties()) {
			var fluidStack = tank.getContents();
			if (fluidStack != null) {
				fluidTagsMap.putIfAbsent(fluidStack.getFluid(), Collections.emptyList());
			}
		}

		return fluidTagsMap;
	}
}
