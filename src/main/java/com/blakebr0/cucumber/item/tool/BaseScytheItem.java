package com.blakebr0.cucumber.item.tool;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.Tier;
import io.github.xponer2.translator.item.ToolItems;
import io.github.xponer2.translator.item.TooltipContext;
import com.blakebr0.cucumber.event.ScytheHarvestCropEvent;
import com.blakebr0.cucumber.helper.CropHelper;
import com.google.common.collect.Multimap;

import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

/**
 * A sword that harvests and replants grown crops in a square (right click) and sweeps nearby mobs.
 */
public class BaseScytheItem extends ItemSword implements TooltipContext.HoverText {
    private final Tier tier;
    private final float attackDamage;
    private final float attackSpeed;
    private final int range;

    public BaseScytheItem(Tier tier, int range) {
        this(tier, range, p -> p);
    }

    public BaseScytheItem(Tier tier, int range, Function<ItemProperties, ItemProperties> properties) {
        super(tier.toolMaterial());
        this.tier = tier;
        this.attackDamage = 4F;
        this.attackSpeed = -2.8F;
        this.range = range;
        properties.apply(new ItemProperties()).applyTo(this);
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World level, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ) {
        var stack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(face), face, stack))
            return EnumActionResult.FAIL;

        if (level.isRemote)
            return EnumActionResult.SUCCESS;

        var harvested = false;

        for (var aoePos : BlockPos.getAllInBoxMutable(pos.add(-this.range, 0, -this.range), pos.add(this.range, 0, this.range))) {
            if (stack.isEmpty())
                break;

            var state = level.getBlockState(aoePos);
            var block = state.getBlock();

            if (block instanceof BlockCrops crop) {
                var seed = CropHelper.getSeedsItem(crop);

                if (crop.isMaxAge(state) && seed != null) {
                    harvested |= harvest(player, level, aoePos.toImmutable(), state, crop.withAge(0), seed, stack);
                }
            }

            if (block instanceof BlockNetherWart && state.getValue(BlockNetherWart.AGE) == 3) {
                harvested |= harvest(player, level, aoePos.toImmutable(), state, state.withProperty(BlockNetherWart.AGE, 0), Items.NETHER_WART, stack);
            }
        }

        if (harvested) {
            level.playSound(null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
        if (player.getCooledAttackStrength(0.5F) >= 0.95F) {
            var level = player.world;
            var range = (this.range >= 2 ? 1.0D + (this.range - 1) * 0.25D : 1.0D);
            var entities = level.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(range, 0.25D, range));
            var source = DamageSource.causePlayerDamage(player);

            for (var aoeEntity : entities) {
                if (aoeEntity != player && aoeEntity != entity && !player.isOnSameTeam(aoeEntity)) {
                    aoeEntity.knockBack(player, 0.4F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                    aoeEntity.attackEntityFrom(source, this.getAttackDamage() * 0.67F);
                }
            }

            level.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
            player.spawnSweepParticles();
        }

        return super.onLeftClickEntity(stack, player, entity);
    }

    @Override
    public float getAttackDamage() {
        return this.attackDamage + this.tier.getAttackDamageBonus();
    }

    public float getAttackSpeed() {
        return this.attackSpeed;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        return ToolItems.mainHandModifiers(slot, ATTACK_DAMAGE_MODIFIER, ATTACK_SPEED_MODIFIER, this.getAttackDamage(), this.attackSpeed);
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

    private static boolean harvest(EntityPlayer player, World level, BlockPos pos, IBlockState state, IBlockState newState, Item seed, ItemStack stack) {
        if (MinecraftForge.EVENT_BUS.post(new ScytheHarvestCropEvent(level, pos, state, stack, player)))
            return false;

        if (MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)))
            return false;

        handleDrops(state, level, pos, seed);
        stack.damageItem(1, player);
        level.setBlockState(pos, newState);

        return true;
    }

    // the drops minus one seed (the crop is replanted)
    private static void handleDrops(IBlockState state, World level, BlockPos pos, Item seed) {
        var drops = NonNullList.<ItemStack>create();
        state.getBlock().getDrops(drops, level, pos, state, 0);

        for (var drop : drops) {
            if (!drop.isEmpty() && drop.getItem() == seed) {
                drop.shrink(1);
                break;
            }
        }

        for (var drop : drops) {
            if (!drop.isEmpty()) {
                net.minecraft.block.Block.spawnAsEntity(level, pos, drop);
            }
        }
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
