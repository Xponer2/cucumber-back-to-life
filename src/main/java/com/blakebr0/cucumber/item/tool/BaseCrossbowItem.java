package com.blakebr0.cucumber.item.tool;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import io.github.xponer2.translator.item.CrossbowItem;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.ItemPropertyFunction;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.iface.ICustomBow;
import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BaseCrossbowItem extends CrossbowItem implements ICustomBow, TooltipContext.HoverText {
    public BaseCrossbowItem(Function<ItemProperties, ItemProperties> properties) {
        super(properties.apply(new ItemProperties()));
    }

    @Override // copied from CrossbowItem#releaseUsing
    public void onPlayerStoppedUsing(ItemStack stack, World level, EntityLivingBase entity, int timeLeft) {
        // change: account for draw speed multiplier
        int i = (int) ((this.getMaxItemUseDuration(stack) - timeLeft) * this.getDrawSpeedMulti(stack));
        this.loadIfCharged(stack, level, entity, i);
    }

    @Override // copied from CrossbowItem#onUseTick
    public void onUsingTick(ItemStack stack, EntityLivingBase entity, int timeLeft) {
        var level = entity.world;
        if (level.isRemote)
            return;

        // change: account for draw speed multiplier
        float f = (float) ((int) ((this.getMaxItemUseDuration(stack) - timeLeft) * this.getDrawSpeedMulti(stack))) / (float) getChargeDuration(stack, entity);
        if (f < 0.2F) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
        }

        if (f >= 0.2F && !this.startSoundPlayed) {
            this.startSoundPlayed = true;
            playSound(level, entity, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.5F);
        }

        if (f >= 0.5F && !this.midLoadSoundPlayed) {
            this.midLoadSoundPlayed = true;
            playSound(level, entity, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.5F);
        }
    }

    @Override
    public EntityArrow customArrow(EntityArrow arrow, ItemStack projectileStack, ItemStack weaponStack) {
        arrow.setDamage(arrow.getDamage() + this.getBonusDamage(weaponStack));
        return arrow;
    }

    @Override
    public boolean hasFOVChange() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        TooltipContext.addInformation(this, stack, world, tooltip, flag);
    }

    // 1.21 tooltip lines
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<ITextComponent> tooltip, ITooltipFlag flag) { }

    public static ItemPropertyFunction getPullPropertyGetter() {
        return (stack, level, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return isCharged(stack) ? 0.0F : (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) * ((ICustomBow) stack.getItem()).getDrawSpeedMulti(stack) / (float) getChargeDuration(stack, entity);
            }
        };
    }

    public static ItemPropertyFunction getPullingPropertyGetter() {
        return (stack, level, entity, seed) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack && !isCharged(stack) ? 1.0F : 0.0F;
    }

    public static ItemPropertyFunction getChargedPropertyGetter() {
        return (stack, level, entity, seed) -> entity != null && isCharged(stack) ? 1.0F : 0.0F;
    }

    // 1.12.2 crossbows load no fireworks
    public static ItemPropertyFunction getFireworkPropertyGetter() {
        return (stack, level, entity, seed) -> 0.0F;
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
