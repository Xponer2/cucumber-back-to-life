package com.blakebr0.cucumber.item.tool;

import java.util.function.Function;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.ItemPropertyFunction;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFishingRod;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * The 1.12.2 fishing rod already registers its "cast" model property.
 */
public class BaseFishingRodItem extends ItemFishingRod {
    public BaseFishingRodItem(Function<ItemProperties, ItemProperties> properties) {
        properties.apply(new ItemProperties()).applyTo(this);
    }

    public static ItemPropertyFunction getCastPropertyGetter() {
        return (stack, level, entity, seed) -> {
            if (entity == null)
                return 0.0F;

            boolean flag = entity.getHeldItemMainhand() == stack;
            boolean flag1 = entity.getHeldItemOffhand() == stack;

            if (entity.getHeldItemMainhand().getItem() instanceof ItemFishingRod) {
                flag1 = false;
            }

            return (flag || flag1) && entity instanceof EntityPlayer player && player.fishEntity != null ? 1.0F : 0.0F;
        };
    }

    // NeoForge ItemAttributeModifierEvent (mods add modifiers to stacks, e.g. upgrades)
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return ItemAttributeModifierEvent.fire(stack, slot, super.getAttributeModifiers(slot, stack));
    }

    // 1.21 item names (1.12.2 translates the name key twice, see ItemNames)
    @Override
    public String getItemStackDisplayName(net.minecraft.item.ItemStack stack) {
        return io.github.xponer2.translator.text.ItemNames.displayName(this, stack);
    }
}
