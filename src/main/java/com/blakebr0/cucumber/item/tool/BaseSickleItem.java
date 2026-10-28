package com.blakebr0.cucumber.item.tool;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.item.ItemProperties;
import io.github.xponer2.translator.item.Tier;
import io.github.xponer2.translator.item.ToolItems;
import io.github.xponer2.translator.item.TooltipContext;
import io.github.xponer2.translator.tags.TagManager;
import com.blakebr0.cucumber.helper.BlockHelper;
import com.blakebr0.cucumber.lib.ModTags;
import com.google.common.collect.Multimap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import io.github.xponer2.translator.item.ItemAttributeModifierEvent;

/**
 * Cuts leaves and plants (block tag cucumber:mineable/sickle) in a cube around the mined block.
 */
public class BaseSickleItem extends ItemTool implements TooltipContext.HoverText {
    private final Tier tier;
    private final float attackDamage;
    private final float attackSpeed;
    private final int range;

    public BaseSickleItem(Tier tier, int range) {
        this(tier, range, p -> p);
    }

    public BaseSickleItem(Tier tier, int range, Function<ItemProperties, ItemProperties> properties) {
        super(4.0F, -3.0F, tier.toolMaterial(), Collections.emptySet());
        this.tier = tier;
        this.attackDamage = 4.0F;
        this.attackSpeed = -3.0F;
        this.range = range;
        properties.apply(new ItemProperties()).applyTo(this);
    }

    public Tier getTier() {
        return this.tier;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        return TagManager.is(state, ModTags.MINEABLE_WITH_SICKLE) ? this.tier.getSpeed() / 2 : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canHarvestBlock(IBlockState state) {
        return TagManager.is(state, ModTags.MINEABLE_WITH_SICKLE) || super.canHarvestBlock(state);
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World level, IBlockState state, BlockPos pos, EntityLivingBase entity) {
        super.onBlockDestroyed(stack, level, state, pos, entity);

        if (level.isRemote)
            return true;

        if (entity instanceof EntityPlayerMP player) {
            this.harvestAOEBlocks(stack, level, pos, state, player);
        }

        return true;
    }

    public float getAttackDamage() {
        return this.attackDamage + this.tier.getAttackDamageBonus();
    }

    public float getAttackSpeed() {
        return this.attackSpeed;
    }

    @SuppressWarnings("deprecation")
    private void harvestAOEBlocks(ItemStack stack, World level, BlockPos pos, IBlockState state, EntityPlayerMP player) {
        if (this.range > 0 && this.canHarvestBlock(state)) {
            var hardness = state.getBlockHardness(level, pos);

            for (var aoePos : BlockPos.getAllInBoxMutable(pos.add(-this.range, -this.range, -this.range), pos.add(this.range, this.range, this.range))) {
                if (stack.isEmpty())
                    return;

                if (!aoePos.equals(pos)) {
                    var aoeState = level.getBlockState(aoePos);
                    if (!this.canHarvestBlock(aoeState))
                        continue;

                    var aoeHardness = aoeState.getBlockHardness(level, aoePos);
                    if (aoeHardness > hardness + 5.0F)
                        continue;

                    var harvested = BlockHelper.harvestAOEBlock(stack, level, player, aoePos.toImmutable());
                    if (harvested && !player.capabilities.isCreativeMode && aoeHardness <= 0.0F && Math.random() < 0.33) {
                        stack.damageItem(1, player);
                    }
                }
            }
        }
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

    // NeoForge ItemAttributeModifierEvent (mods add modifiers to stacks, e.g. upgrades)
    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        return ItemAttributeModifierEvent.fire(stack, slot, super.getAttributeModifiers(slot, stack));
    }
}
