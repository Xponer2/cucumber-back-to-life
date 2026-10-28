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
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

/**
 * Armor of a 1.21 armor material: the 1.12.2 material gives armor, toughness, enchantability, sound and texture; the
 * knockback resistance is added as a modifier, the repair item is the material's ingredient.
 */
public class BaseArmorItem extends ItemArmor implements TooltipContext.HoverText {
    // one id per armor slot (feet, legs, chest, head), so the pieces add up
    private static final java.util.UUID[] KNOCKBACK_MODIFIERS = {
            java.util.UUID.fromString("3b7f1a4e-2c5d-4e8f-9a1b-6c7d8e9f0a11"), java.util.UUID.fromString("3b7f1a4e-2c5d-4e8f-9a1b-6c7d8e9f0a12"),
            java.util.UUID.fromString("3b7f1a4e-2c5d-4e8f-9a1b-6c7d8e9f0a13"), java.util.UUID.fromString("3b7f1a4e-2c5d-4e8f-9a1b-6c7d8e9f0a14") };
    private final ArmorMaterialData material;
    private final ArmorType type;

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
        super(material.value().toolMaterial(), 0, type.getSlot());
        this.material = material.value();
        this.type = type;
        properties.apply(new ItemProperties().durability(type.getDurability(maxDamageFactor))).applyTo(this);
    }

    public ArmorMaterialData getMaterial() {
        return this.material;
    }

    public ArmorType getType() {
        return this.type;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        var modifiers = super.getItemAttributeModifiers(slot);
        if (slot == this.armorType && this.material.knockbackResistance() > 0) {
            modifiers.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(),
                    new AttributeModifier(KNOCKBACK_MODIFIERS[slot.getIndex()], "Armor knockback resistance", this.material.knockbackResistance(), 0));
        }
        return modifiers;
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return this.material.repairIngredient().get().apply(repair);
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
