package com.blakebr0.cucumber.item;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.TooltipContext;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public class BaseItem extends Item implements TooltipContext.HoverText {
	public BaseItem() {
		this(p -> p);
	}

	public BaseItem(Function<ItemProperties, ItemProperties> properties) {
		properties.apply(new ItemProperties()).applyTo(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
		TooltipContext.addInformation(this, stack, world, tooltip, flag);
	}

	// 1.21 tooltip lines
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<ITextComponent> tooltip, ITooltipFlag flag) { }

	// NeoForge ItemAttributeModifierEvent (mods add modifiers to stacks, e.g. upgrades)
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		return ItemAttributeModifierEvent.fire(stack, slot, super.getAttributeModifiers(slot, stack));
	}
}
