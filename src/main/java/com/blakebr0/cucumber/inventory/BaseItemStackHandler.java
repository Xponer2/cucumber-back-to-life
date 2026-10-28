package com.blakebr0.cucumber.inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.github.xponer2.translator.recipe.CraftingInput;
import io.github.xponer2.translator.registry.HolderLookup;
import com.blakebr0.cucumber.crafting.ShapelessCraftingInput;

import net.minecraftforge.items.ItemStackHandler;
import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class BaseItemStackHandler extends ItemStackHandler {
    // 1.12.2 stores the count of a stack as a byte: slots above 127 items keep the real count in this tag
    private static final String LARGE_COUNT = "CucumberCount";

    private final OnContentsChangedFunction onContentsChanged;
    private final Map<Integer, Integer> slotSizeMap;
    private CanInsertFunction canInsert = null;
    private CanExtractFunction canExtract = null;
    private int maxStackSize = 64;
    private int[] outputSlots = null;

    protected BaseItemStackHandler(int size, OnContentsChangedFunction onContentsChanged) {
        super(size);
        this.onContentsChanged = onContentsChanged;
        this.slotSizeMap = new HashMap<>();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return this.insertItem(slot, stack, simulate, false);
    }

    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate, boolean container) {
        if (!container && this.outputSlots != null && ArrayUtils.contains(this.outputSlots, slot))
            return stack;

        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return this.extractItem(slot, amount, simulate, false);
    }

    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean container) {
        if (!container) {
            if (this.canExtract != null && !this.canExtract.apply(slot))
                return ItemStack.EMPTY;

            if (this.outputSlots != null && !ArrayUtils.contains(this.outputSlots, slot))
                return ItemStack.EMPTY;
        }

        return super.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotSizeMap.containsKey(slot) ? this.slotSizeMap.get(slot) : this.maxStackSize;
    }

    @Override
    public int getStackLimit(int slot, ItemStack stack) {
        return super.getStackLimit(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return this.canInsert == null || this.canInsert.apply(slot, stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (this.onContentsChanged != null) {
            this.onContentsChanged.apply(slot);
        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        var items = new NBTTagList();
        for (int i = 0; i < this.stacks.size(); ++i) {
            var stack = this.stacks.get(i);
            if (!stack.isEmpty()) {
                var item = new NBTTagCompound();
                item.setInteger("Slot", i);
                stack.writeToNBT(item);
                if (stack.getCount() > Byte.MAX_VALUE) {
                    item.setInteger(LARGE_COUNT, stack.getCount());
                }
                items.appendTag(item);
            }
        }
        var nbt = new NBTTagCompound();
        nbt.setTag("Items", items);
        nbt.setInteger("Size", this.stacks.size());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        var size = nbt.hasKey("Size", 3) ? nbt.getInteger("Size") : this.stacks.size();
        this.setSize(Math.max(size, this.stacks.size()));
        var items = nbt.getTagList("Items", 10);
        for (int i = 0; i < items.tagCount(); ++i) {
            var item = items.getCompoundTagAt(i);
            int slot = item.getInteger("Slot");
            if (slot >= 0 && slot < this.stacks.size()) {
                var stack = new ItemStack(item);
                if (item.hasKey(LARGE_COUNT, 3)) {
                    stack.setCount(item.getInteger(LARGE_COUNT));
                }
                this.stacks.set(slot, stack);
            }
        }
        this.onLoad();
    }

    // 1.21 signatures
    public NBTTagCompound serializeNBT(HolderLookup.Provider lookup) {
        return this.serializeNBT();
    }

    public void deserializeNBT(HolderLookup.Provider lookup, NBTTagCompound nbt) {
        this.deserializeNBT(nbt);
    }

    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public int[] getOutputSlots() {
        return this.outputSlots;
    }

    public void setDefaultSlotLimit(int size) {
        this.maxStackSize = size;
    }

    /**
     * Adds a new slot limit for the specified slot
     * <br>
     * If the size is above 64, it must be a multiple of 64
     */
    public void addSlotLimit(int slot, int size) {
        if (size > 64 && size % 64 != 0) {
            throw new IllegalArgumentException("Slot limits above 64 must be a multiple of 64");
        }

        this.slotSizeMap.put(slot, size);
    }

    public void setCanInsert(CanInsertFunction canInsert) {
        this.canInsert = canInsert;
    }

    public void setCanExtract(CanExtractFunction canExtract) {
        this.canExtract = canExtract;
    }

    public void setOutputSlots(int... slots) {
        this.outputSlots = slots;
    }

    public RecipeInventory toRecipeInventory() {
        return toRecipeInventory(0, this.stacks.size());
    }

    /**
     * Creates a RecipeInventory for use in recipe matching with only a specific range of slots
     * <br>
     * This implementation will NOT copy the items in the inventory so don't modify them
     *
     * @param start the first index of the recipe inputs in the inventory
     * @param size  the amount of recipe input slots
     * @return a new RecipeInventory
     */
    public RecipeInventory toRecipeInventory(int start, int size) {
        return new RecipeInventory(this, start, size);
    }

    /**
     * Creates a {@link CraftingInput} of this inventory
     *
     * @param width  the width of the input
     * @param height the height of the input
     * @return the new {@link CraftingInput}
     */
    public CraftingInput toCraftingInput(int width, int height) {
        return CraftingInput.of(width, height, this.stacks);
    }

    /**
     * Creates a {@link CraftingInput} of this inventory
     *
     * @return the new {@link CraftingInput}
     */
    public CraftingInput toShapelessCraftingInput() {
        return new ShapelessCraftingInput(this.stacks);
    }

    /**
     * Creates a {@link CraftingInput} using a subset of this inventory
     *
     * @param width      the width of the input
     * @param height     the height of the input
     * @param startIndex the start index of this subset
     * @param endIndex   the end index of this subset
     * @return the new {@link CraftingInput}
     */
    public CraftingInput toCraftingInput(int width, int height, int startIndex, int endIndex) {
        return CraftingInput.of(width, height, this.stacks.subList(startIndex, endIndex));
    }

    /**
     * Creates a {@link CraftingInput} using a subset of this inventory
     *
     * @param startIndex the start index of this subset
     * @param endIndex   the end index of this subset
     * @return the new {@link CraftingInput}
     */
    public CraftingInput toShapelessCraftingInput(int startIndex, int endIndex) {
        return new ShapelessCraftingInput(this.stacks.subList(startIndex, endIndex));
    }

    /**
     * Creates a deep copy of this BaseItemStackHandler, including new copies of the items
     *
     * @return the copy of this BaseItemStackHandler
     */
    public BaseItemStackHandler copy() {
        var newInventory = new BaseItemStackHandler(this.getSlots(), this.onContentsChanged);

        newInventory.setDefaultSlotLimit(this.maxStackSize);
        newInventory.setCanInsert(this.canInsert);
        newInventory.setCanExtract(this.canExtract);
        newInventory.setOutputSlots(this.outputSlots);

        this.slotSizeMap.forEach(newInventory::addSlotLimit);

        for (int i = 0; i < this.getSlots(); i++) {
            var stack = this.getStackInSlot(i);

            newInventory.setStackInSlot(i, stack.copy());
        }

        return newInventory;
    }

    public static BaseItemStackHandler create(int size) {
        return create(size, builder -> {});
    }

    public static BaseItemStackHandler create(int size, Consumer<BaseItemStackHandler> builder) {
        return create(size, null, builder);
    }

    public static BaseItemStackHandler create(int size, OnContentsChangedFunction onContentsChanged, Consumer<BaseItemStackHandler> builder) {
        var handler = new BaseItemStackHandler(size, onContentsChanged);
        builder.accept(handler);
        return handler;
    }
}
