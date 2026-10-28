package com.blakebr0.cucumber.item.tool;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.Tier;
import io.github.xponer2.translator.item.ToolItems;
import io.github.xponer2.translator.item.TooltipContext;
import com.google.common.collect.Multimap;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

/**
 * Pickaxe, axe and shovel in one: the three 1.12.2 tool classes at the tier's level, the materials those tools are
 * fast on, and the shovel's path making (1.12.2 has no log stripping).
 */
public class BasePaxelItem extends ItemTool implements TooltipContext.HoverText {
    private static final float ATTACK_DAMAGE = 4.0F;
    private static final float ATTACK_SPEED = -3.2F;

    private final Tier tier;

    public BasePaxelItem(Tier tier) {
        this(tier, p -> p);
    }

    public BasePaxelItem(Tier tier, Function<ItemProperties, ItemProperties> properties) {
        super(ATTACK_DAMAGE, ATTACK_SPEED, tier.toolMaterial(), Collections.emptySet());
        this.tier = tier;
        properties.apply(new ItemProperties().durability((int) (tier.getUses() * 1.5))).applyTo(this);
        var level = tier.harvestLevel();
        this.setHarvestLevel("pickaxe", level);
        this.setHarvestLevel("axe", level);
        this.setHarvestLevel("shovel", level);
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        var material = state.getMaterial();
        if (material == Material.ROCK || material == Material.IRON || material == Material.ANVIL
                || material == Material.WOOD || material == Material.PLANTS || material == Material.VINE || material == Material.GOURD
                || material == Material.GROUND || material == Material.GRASS || material == Material.SAND
                || material == Material.SNOW || material == Material.CRAFTED_SNOW || material == Material.CLAY) {
            return this.efficiency;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state) {
        var material = state.getMaterial();
        if (material == Material.ROCK || material == Material.IRON || material == Material.ANVIL) {
            var required = state.getBlock().getHarvestLevel(state);
            return required <= this.tier.harvestLevel();
        }
        return material == Material.SNOW || material == Material.CRAFTED_SNOW || super.canHarvestBlock(state);
    }

    // shovel: grass to grass path
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        var stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, stack)) {
            return EnumActionResult.FAIL;
        }
        var state = world.getBlockState(pos);
        if (facing != EnumFacing.DOWN && state.getBlock() == Blocks.GRASS && world.getBlockState(pos.up()).getMaterial() == Material.AIR) {
            world.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!world.isRemote) {
                world.setBlockState(pos, Blocks.GRASS_PATH.getDefaultState(), 11);
                stack.damageItem(1, player);
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        return ToolItems.mainHandModifiers(slot, ATTACK_DAMAGE_MODIFIER, ATTACK_SPEED_MODIFIER, ATTACK_DAMAGE + this.tier.getAttackDamageBonus(), ATTACK_SPEED);
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
