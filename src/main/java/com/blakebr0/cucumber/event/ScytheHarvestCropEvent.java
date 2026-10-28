package com.blakebr0.cucumber.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ScytheHarvestCropEvent extends Event {
    private final World level;
    private final BlockPos pos;
    private final IBlockState state;
    private final ItemStack stack;
    private final EntityPlayer player;

    public ScytheHarvestCropEvent(World level, BlockPos pos, IBlockState state, ItemStack stack, EntityPlayer player) {
        this.pos = pos;
        this.level = level;
        this.state = state;
        this.stack = stack;
        this.player = player;
    }

    public World getLevel()
    {
        return this.level;
    }

    public BlockPos getPos()
    {
        return this.pos;
    }

    public IBlockState getState()
    {
        return this.state;
    }

    public ItemStack getItemStack() {
        return this.stack;
    }

    public EntityPlayer getPlayer() {
        return this.player;
    }
}
