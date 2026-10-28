package com.blakebr0.cucumber.item.tool;

import java.util.function.Function;

import io.github.xponer2.translator.item.ItemProperties;

import net.minecraft.item.ItemShears;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class BaseShearsItem extends ItemShears {
    // the 1.12.2 shears already are a tool for shears blocks (1.21 adds the tool component)
    public BaseShearsItem(Function<ItemProperties, ItemProperties> properties) {
        super();
        properties.apply(new ItemProperties()).applyTo(this);
    }

    // NeoForge ItemAttributeModifierEvent (mods add modifiers to stacks, e.g. upgrades)
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return ItemAttributeModifierEvent.fire(stack, slot, super.getAttributeModifiers(slot, stack));
    }
}
