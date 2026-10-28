package com.blakebr0.cucumber.crafting;

import java.util.List;

import io.github.xponer2.translator.recipe.CraftingInput;

import net.minecraft.item.ItemStack;

public class ShapelessCraftingInput extends CraftingInput {
    public ShapelessCraftingInput(List<ItemStack> items) {
        super(items.size(), 1, items);
    }
}
