package com.blakebr0.cucumber.item;

import java.util.List;
import java.util.function.Function;

import io.github.xponer2.translator.component.StackComponents;
import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.lib.Tooltips;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public class BaseReusableItem extends BaseItem {
	private final boolean unbreakable;
	private final boolean tooltip;

	public BaseReusableItem(int uses) {
		this(uses, p -> p);
	}

	public BaseReusableItem(Function<ItemProperties, ItemProperties> properties) {
		this(true, properties);
	}

	public BaseReusableItem(boolean tooltip, Function<ItemProperties, ItemProperties> properties) {
		this(0, tooltip, properties);
	}

	public BaseReusableItem(int uses, Function<ItemProperties, ItemProperties> properties) {
		this(uses, true, properties);
	}

	public BaseReusableItem(int uses, boolean tooltip, Function<ItemProperties, ItemProperties> properties) {
		super(properties.compose(p -> p.durability(Math.max(uses - 1, 0)).setNoRepair()));
		this.unbreakable = uses < 1;
		this.tooltip = tooltip;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		var copy = StackComponents.copyWithCount(stack, 1);

		if (this.unbreakable)
			return copy;

		var unbreaking = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
		if (Math.random() > (1.0F / (unbreaking + 1)))
			return copy;

		var newDamage = stack.getItemDamage() + 1;
		if (newDamage > stack.getMaxDamage())
			return ItemStack.EMPTY;

		copy.setItemDamage(newDamage);

		return copy;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<ITextComponent> tooltip, ITooltipFlag flag) {
		if (this.tooltip) {
			if (!this.unbreakable) {
				var damage = stack.getMaxDamage() - stack.getItemDamage() + 1;
				if (damage == 1) {
					tooltip.add(Tooltips.ONE_USE_LEFT.build());
				} else {
					tooltip.add(Tooltips.USES_LEFT.args(damage).build());
				}
			} else {
				tooltip.add(Tooltips.UNLIMITED_USES.build());
			}
		}
	}
}
