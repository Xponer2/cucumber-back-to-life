package com.blakebr0.cucumber.inventory.slot;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class OutputSlot extends SlotItemHandler {
	public OutputSlot(IItemHandler inventory, int slotNumber, int xPosition, int yPosition) {
		super(inventory, slotNumber, xPosition, yPosition);
	}

	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
}
