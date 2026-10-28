package com.blakebr0.cucumber.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

@FunctionalInterface
public interface CanInsertFunction {
    boolean apply(int slot, ItemStack stack);

    @FunctionalInterface
    interface Sided {
        boolean apply(int slot, ItemStack stack, EnumFacing direction);
    }
}
