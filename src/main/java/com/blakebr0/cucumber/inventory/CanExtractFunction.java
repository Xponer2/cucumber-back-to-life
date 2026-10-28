package com.blakebr0.cucumber.inventory;

import net.minecraft.util.EnumFacing;

@FunctionalInterface
public interface CanExtractFunction {
    boolean apply(int slot);

    @FunctionalInterface
    interface Sided {
        boolean apply(int slot, EnumFacing direction);
    }
}
