package com.blakebr0.cucumber.item.tool;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.Tier;
import io.github.xponer2.translator.item.ToolItems;
import io.github.xponer2.translator.item.TooltipContext;
import com.google.common.collect.Multimap;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

public class BaseAxeItem extends ItemAxe implements TooltipContext.HoverText {
    private final Tier tier;
    private final float attackDamage;
    private final float attackSpeed;

    public BaseAxeItem(Tier tier) {
        this(tier, 6.0F, -3.0F, p -> p);
    }

    public BaseAxeItem(Tier tier, Function<ItemProperties, ItemProperties> properties) {
        this(tier, 6.0F, -3.0F, properties);
    }

    public BaseAxeItem(Tier tier, float attackDamage, float attackSpeed, Function<ItemProperties, ItemProperties> properties) {
        super(tier.toolMaterial(), attackDamage + tier.getAttackDamageBonus(), attackSpeed);
        this.tier = tier;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        properties.apply(new ItemProperties()).applyTo(this);
    }

    public Tier getTier() {
        return this.tier;
    }

    public float getAttackDamage() {
        return this.attackDamage + this.tier.getAttackDamageBonus();
    }

    public float getAttackSpeed() {
        return this.attackSpeed;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        return ToolItems.mainHandModifiers(slot, ATTACK_DAMAGE_MODIFIER, ATTACK_SPEED_MODIFIER, this.getAttackDamage(), this.getAttackSpeed());
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        return ToolItems.isRepairable(this.tier, repair);
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
