package com.blakebr0.cucumber.item.tool;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.ItemPropertyFunction;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.iface.ICustomBow;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * A bow with a draw speed multiplier and bonus arrow damage ({@link ICustomBow}). The 1.12.2 bow registers its "pull"
 * and "pulling" model properties; the client replaces "pull" with {@link #getPullPropertyGetter()} (draw speed).
 */
public class BaseBowItem extends ItemBow implements ICustomBow, TooltipContext.HoverText {
    public BaseBowItem(Function<ItemProperties, ItemProperties> properties) {
        properties.apply(new ItemProperties()).applyTo(this);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World level, EntityLivingBase entity, int timeLeft) {
        if (!(entity instanceof EntityPlayer player))
            return;

        var infinite = player.capabilities.isCreativeMode || EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY, stack) > 0;
        var ammo = findArrows(player);
        // change: account for draw speed multiplier
        int charge = (int) ((this.getMaxItemUseDuration(stack) - timeLeft) * this.getDrawSpeedMulti(stack));
        charge = ForgeEventFactory.onArrowLoose(stack, level, player, charge, !ammo.isEmpty() || infinite);
        if (charge < 0 || (ammo.isEmpty() && !infinite))
            return;

        if (ammo.isEmpty()) {
            ammo = new ItemStack(Items.ARROW);
        }

        float velocity = getArrowVelocity(charge);
        if (velocity < 0.1)
            return;

        var keepAmmo = player.capabilities.isCreativeMode || (ammo.getItem() instanceof ItemArrow arrowItem && arrowItem.isInfinite(ammo, stack, player));

        if (!level.isRemote) {
            var arrowItem = ammo.getItem() instanceof ItemArrow a ? a : (ItemArrow) Items.ARROW;
            var arrow = this.customizeArrow(arrowItem.createArrow(level, ammo, player));
            arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, velocity * 3.0F, 1.0F);

            if (velocity == 1.0F)
                arrow.setIsCritical(true);

            var power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
            if (power > 0)
                arrow.setDamage(arrow.getDamage() + power * 0.5D + 0.5D);

            var punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
            if (punch > 0)
                arrow.setKnockbackStrength(punch);

            if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack) > 0)
                arrow.setFire(100);

            arrow.setDamage(arrow.getDamage() + this.getBonusDamage(stack));
            stack.damageItem(1, player);

            if (keepAmmo || player.capabilities.isCreativeMode && (ammo.getItem() == Items.SPECTRAL_ARROW || ammo.getItem() == Items.TIPPED_ARROW)) {
                arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
            }

            io.github.xponer2.translator.util.Platform.addFreshEntity(level, arrow);
        }

        level.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);

        if (!keepAmmo && !player.capabilities.isCreativeMode) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.inventory.deleteStack(ammo);
            }
        }

        player.addStat(StatList.getObjectUseStats(this));
    }

    // off hand, main hand, then the inventory, like the 1.12.2 bow
    private static ItemStack findArrows(EntityPlayer player) {
        if (player.getHeldItemOffhand().getItem() instanceof ItemArrow)
            return player.getHeldItemOffhand();

        if (player.getHeldItemMainhand().getItem() instanceof ItemArrow)
            return player.getHeldItemMainhand();

        for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
            var stack = player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemArrow)
                return stack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean hasFOVChange() {
        return true;
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
                return entity.getActiveItemStack() != stack ? 0.0F : (float) (stack.getMaxItemUseDuration() - entity.getItemInUseCount()) * ((ICustomBow) stack.getItem()).getDrawSpeedMulti(stack) / 20.0F;
            }
        };
    }

    public static ItemPropertyFunction getPullingPropertyGetter() {
        return (stack, level, entity, seed) -> entity != null && entity.isHandActive() && entity.getActiveItemStack() == stack ? 1.0F : 0.0F;
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
