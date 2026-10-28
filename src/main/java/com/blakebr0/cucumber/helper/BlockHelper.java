package com.blakebr0.cucumber.helper;

import javax.annotation.Nullable;

import com.blakebr0.cucumber.tileentity.BaseInventoryTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public final class BlockHelper {
    public static RayTraceResult rayTraceBlocks(World level, EntityPlayer player) {
        return rayTraceBlocks(level, player, false);
    }

    /**
     * The block the player looks at within its reach; {@code stopOnLiquid} hits liquids too (1.21 ClipContext.Fluid).
     */
    @Nullable
    public static RayTraceResult rayTraceBlocks(World level, EntityPlayer player, boolean stopOnLiquid) {
        var attribute = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE);
        var reach = attribute != null ? attribute.getAttributeValue() : 4.5D;
        var eyePos = player.getPositionEyes(1.0F);
        var look = player.getLook(1.0F);
        var end = eyePos.add(look.x * reach, look.y * reach, look.z * reach);
        return level.rayTraceBlocks(eyePos, end, stopOnLiquid, !stopOnLiquid, false);
    }

    public static boolean harvestBlock(ItemStack stack, World level, EntityPlayerMP player, BlockPos pos) {
        return harvestBlock(stack, level, player, pos, true);
    }

    /**
     * Breaks a block as the player would with the item (break event, drops, experience, item use).
     */
    public static boolean harvestBlock(ItemStack stack, World level, EntityPlayerMP player, BlockPos pos, boolean playEvent) {
        if (level.isRemote)
            return true;

        var type = player.interactionManager.getGameType();
        var exp = ForgeHooks.onBlockBreakEvent(level, type, player, pos);
        if (exp == -1)
            return false;

        var state = level.getBlockState(pos);
        var block = state.getBlock();
        var tile = level.getTileEntity(pos);

        if (playEvent) {
            level.playEvent(2001, pos, Block.getStateId(state));
        }

        if (player.isCreative()) {
            block.onBlockHarvested(level, pos, state, player);
            if (block.removedByPlayer(state, level, pos, player, false)) {
                block.onPlayerDestroy(level, pos, state);
            }
            return true;
        }

        stack.onBlockDestroyed(level, state, pos, player);
        block.onBlockHarvested(level, pos, state, player);
        var canHarvest = block.canHarvestBlock(level, pos, player);
        if (block.removedByPlayer(state, level, pos, player, canHarvest)) {
            block.onPlayerDestroy(level, pos, state);
            if (canHarvest) {
                block.harvestBlock(level, player, pos, state, tile, stack);
            }
            if (exp > 0) {
                block.dropXpOnBlockBreak(level, pos, exp);
            }
        }

        return true;
    }

    public static boolean harvestAOEBlock(ItemStack stack, World level, EntityPlayerMP player, BlockPos pos) {
        if (harvestBlock(stack, level, player, pos, false)) {
            player.connection.sendPacket(new SPacketBlockChange(level, pos));
            return true;
        }

        return false;
    }

    public static boolean destroyBlock(IBlockState state, World level, EntityPlayer player, BlockPos pos) {
        var block = state.getBlock();
        var canHarvest = !player.isCreative() && block.canHarvestBlock(level, pos, player);
        var destroyed = block.removedByPlayer(state, level, pos, player, canHarvest);
        if (destroyed) {
            block.onPlayerDestroy(level, pos, state);
        }

        return destroyed;
    }

    /**
     * The comparator signal of a Cucumber inventory, like 1.12.2 {@code Container#calcRedstoneFromInventory}.
     */
    public static int getRedstoneSignalFromInventory(@Nullable TileEntity entity) {
        if (entity instanceof BaseInventoryTileEntity tile) {
            var inventory = tile.getInventory();
            float f = 0.0F;
            int filled = 0;

            for (int i = 0; i < inventory.getSlots(); ++i) {
                var stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    f += (float) stack.getCount() / (float) inventory.getStackLimit(i, stack);
                    filled++;
                }
            }

            f /= (float) inventory.getSlots();
            return MathHelper.floor(f * 14.0F) + (filled > 0 ? 1 : 0);
        }

        return 0;
    }
}
