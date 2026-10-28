package com.blakebr0.cucumber.item;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ArmorMaterialData;
import io.github.xponer2.translator.item.ArmorType;
import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.TooltipContext;
import io.github.xponer2.translator.registry.Holder;
import com.google.common.collect.Multimap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import io.github.xponer2.translator.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

public class BaseArmorItem extends ArmorItem implements TooltipContext.HoverText {
    @Deprecated
    public BaseArmorItem(Holder<ArmorMaterialData> material, ArmorType type) {
        this(material, type, 0);
    }

    public BaseArmorItem(Holder<ArmorMaterialData> material, ArmorType type, int maxDamageFactor) {
        this(material, type, maxDamageFactor, p -> p);
    }

    public BaseArmorItem(Holder<ArmorMaterialData> material, ArmorType type, Function<ItemProperties, ItemProperties> properties) {
        this(material, type, 0, properties);
    }

    public BaseArmorItem(Holder<ArmorMaterialData> material, ArmorType type, int maxDamageFactor, Function<ItemProperties, ItemProperties> properties) {
        super(material, type, properties.apply(new ItemProperties().durability(type.getDurability(maxDamageFactor))));
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

    // 1.21 item names (1.12.2 translates the name key twice, see ItemNames)
    @Override
    public String getItemStackDisplayName(net.minecraft.item.ItemStack stack) {
        return io.github.xponer2.translator.text.ItemNames.displayName(this, stack);
    }
}
