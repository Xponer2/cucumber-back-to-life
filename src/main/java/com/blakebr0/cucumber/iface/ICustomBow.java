package com.blakebr0.cucumber.iface;

import net.minecraft.item.ItemStack;

public interface ICustomBow {
	default float getDrawSpeedMulti(ItemStack stack) {
		return 1.0F;
	}

	default float getBonusDamage(ItemStack stack) {
		return 0.0F;
	}

	boolean hasFOVChange();
}
