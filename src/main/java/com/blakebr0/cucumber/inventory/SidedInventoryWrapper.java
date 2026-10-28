package com.blakebr0.cucumber.inventory;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.items.IItemHandler;

public class SidedInventoryWrapper implements IItemHandler {
    private final BaseItemStackHandler inventory;
    private final EnumFacing direction;
    private final CanInsertFunction.Sided canInsert;
    private final CanExtractFunction.Sided canExtract;

    private SidedInventoryWrapper(BaseItemStackHandler inventory, EnumFacing direction, @Nullable CanInsertFunction.Sided canInsert, @Nullable CanExtractFunction.Sided canExtract) {
        this.inventory = inventory;
        this.direction = direction;
        this.canInsert = canInsert;
        this.canExtract = canExtract;
    }

    @Override
    public int getSlots() {
        return this.inventory.getSlots();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (this.canInsert != null && !this.canInsert.apply(slot, stack, this.direction))
            return stack;

        return this.inventory.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (this.canExtract != null && !this.canExtract.apply(slot, this.direction))
            return ItemStack.EMPTY;

        return this.inventory.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.canInsert != null && this.canInsert.apply(slot, stack, this.direction) && this.inventory.isItemValid(slot, stack);
    }

    public static SidedInventoryWrapper[] create(BaseItemStackHandler inventory, List<EnumFacing> directions, CanInsertFunction.Sided canInsert, CanExtractFunction.Sided canExtract) {
        var wrappers = new SidedInventoryWrapper[directions.size()];

        for (var i = 0; i < directions.size(); i++) {
            wrappers[i] = new SidedInventoryWrapper(inventory, directions.get(i), canInsert, canExtract);
        }

        return wrappers;
    }
}
