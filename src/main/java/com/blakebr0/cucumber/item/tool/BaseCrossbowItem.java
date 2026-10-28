package com.blakebr0.cucumber.item.tool;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.ItemPropertyFunction;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.iface.ICustomBow;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;
import com.google.common.collect.Multimap;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.ai.attributes.AttributeModifier;

/**
 * A crossbow (1.12.2 has none): hold use to load an arrow (25 ticks, divided by the draw speed multiplier), use again
 * to shoot it. The loaded arrow is kept in the stack NBT ({@code ChargedProjectiles}, {@code Charged}). Fireworks and
 * the 1.14+ crossbow enchantments and sounds don't exist in 1.12.2: only arrows are loaded, and the sounds are close
 * 1.12.2 ones.
 */
public class BaseCrossbowItem extends Item implements ICustomBow, TooltipContext.HoverText {
    private static final int CHARGE_DURATION = 25;
    private static final String CHARGED = "Charged";
    private static final String PROJECTILES = "ChargedProjectiles";

    private boolean startSoundPlayed = false;
    private boolean midLoadSoundPlayed = false;

    public BaseCrossbowItem(Function<ItemProperties, ItemProperties> properties) {
        this.setMaxStackSize(1);
        this.setMaxDamage(465);
        properties.apply(new ItemProperties()).applyTo(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World level, EntityPlayer player, EnumHand hand) {
        var stack = player.getHeldItem(hand);

        if (isCharged(stack)) {
            this.shoot(level, player, stack);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        if (!findArrows(player).isEmpty() || player.capabilities.isCreativeMode) {
            this.startSoundPlayed = false;
            this.midLoadSoundPlayed = false;
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
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
    public void onPlayerStoppedUsing(ItemStack stack, World level, EntityLivingBase entity, int timeLeft) {
        // change: account for draw speed multiplier
        int i = (int) ((this.getMaxItemUseDuration(stack) - timeLeft) * this.getDrawSpeedMulti(stack));
        if (i >= getChargeDuration(stack, entity) && !isCharged(stack) && entity instanceof EntityPlayer player && tryLoadProjectile(player, stack)) {
            playSound(level, entity, SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, 1.0F);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getItemEnchantability() {
        return 1;
    }

    private static boolean tryLoadProjectile(EntityPlayer player, ItemStack crossbow) {
        var ammo = findArrows(player);
        var creative = player.capabilities.isCreativeMode;
        if (ammo.isEmpty()) {
            if (!creative)
                return false;
            ammo = new ItemStack(Items.ARROW);
        }

        var loaded = ammo.copy();
        loaded.setCount(1);
        if (!creative) {
            ammo.shrink(1);
            if (ammo.isEmpty()) {
                player.inventory.deleteStack(ammo);
            }
        }

        var projectiles = new NBTTagList();
        projectiles.appendTag(loaded.writeToNBT(new NBTTagCompound()));
        var tag = crossbow.hasTagCompound() ? crossbow.getTagCompound() : new NBTTagCompound();
        tag.setTag(PROJECTILES, projectiles);
        tag.setBoolean(CHARGED, true);
        crossbow.setTagCompound(tag);
        return true;
    }

    private void shoot(World level, EntityPlayer player, ItemStack crossbow) {
        var tag = crossbow.getTagCompound();
        var projectiles = tag.getTagList(PROJECTILES, 10);

        if (!level.isRemote) {
            for (int i = 0; i < projectiles.tagCount(); i++) {
                var ammo = new ItemStack(projectiles.getCompoundTagAt(i));
                var arrowItem = ammo.getItem() instanceof ItemArrow a ? a : (ItemArrow) Items.ARROW;
                var arrow = arrowItem.createArrow(level, ammo, player);
                arrow.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 3.15F, 1.0F);
                arrow.setIsCritical(true);
                arrow.setDamage(arrow.getDamage() + this.getBonusDamage(crossbow));
                if (player.capabilities.isCreativeMode) {
                    arrow.pickupStatus = EntityArrow.PickupStatus.CREATIVE_ONLY;
                }
                level.spawnEntity(arrow);
                crossbow.damageItem(1, player);
            }
        }

        level.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS,
                1.0F, 1.0F / (itemRand.nextFloat() * 0.4F + 1.2F) + 0.5F);
        player.addStat(StatList.getObjectUseStats(this));

        tag.removeTag(PROJECTILES);
        tag.setBoolean(CHARGED, false);
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

    private static void playSound(World level, EntityLivingBase entity, SoundEvent sound, float volume) {
        level.playSound(null, entity.posX, entity.posY, entity.posZ, sound, SoundCategory.PLAYERS, volume, 1.0F);
    }

    public static boolean isCharged(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean(CHARGED);
    }

    public static int getChargeDuration(ItemStack stack, @Nullable EntityLivingBase entity) {
        return CHARGE_DURATION;
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
}
