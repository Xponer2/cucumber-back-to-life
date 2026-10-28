package com.blakebr0.cucumber.item;

import java.util.function.Function;

import io.github.xponer2.translator.item.ItemProperties;

import net.minecraft.item.ItemStack;

public class BaseShinyItem extends BaseItem {
    public BaseShinyItem() {
        super();
    }

    public BaseShinyItem(Function<ItemProperties, ItemProperties> properties) {
        super(properties);
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return true;
    }
}
