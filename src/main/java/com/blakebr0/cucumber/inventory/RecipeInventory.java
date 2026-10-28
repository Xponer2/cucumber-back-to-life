package com.blakebr0.cucumber.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * A view of a range of slots of an item handler as a 1.12.2 inventory, for recipe matching. The 1.21 method names
 * (getItem, setItem, removeItem) are kept next to the 1.12.2 ones.
 */
public class RecipeInventory implements IInventory {
    private final IItemHandlerModifiable inventory;
    private final int start;
    private final int size;

    public RecipeInventory(IItemHandlerModifiable inventory) {
        this(inventory, 0, inventory.getSlots());
    }

    public RecipeInventory(IItemHandlerModifiable inventory, int start, int size) {
        this.inventory = inventory;
        this.start = start;
        this.size = size;
    }

    @Override
    public int getSizeInventory() {
        return this.size;
    }

    public ItemStack getItem(int slot) {
        return this.inventory.getStackInSlot(slot + this.start);
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.getItem(slot);
    }

    public ItemStack removeItem(int slot, int count) {
        var stack = this.getItem(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.splitStack(count);
    }

    @Override
    public ItemStack decrStackSize(int slot, int count) {
        return this.removeItem(slot, count);
    }

    public void setItem(int slot, ItemStack stack) {
        this.inventory.setStackInSlot(slot + this.start, stack);
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        this.setItem(slot, stack);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        var stack = this.getItem(slot);
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        this.setItem(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public boolean isEmpty() {
        for (int i = this.start; i < this.start + this.size; i++) {
            if (!this.inventory.getStackInSlot(i).isEmpty())
                return false;
        }

        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return this.inventory.isItemValid(slot + this.start, stack);
    }

    @Override
    public void clear() {
        for (int i = this.start; i < this.start + this.size; i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public void markDirty() { }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) { }

    @Override
    public void closeInventory(EntityPlayer player) { }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) { }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public String getName() {
        return "recipe_inventory";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }
}
