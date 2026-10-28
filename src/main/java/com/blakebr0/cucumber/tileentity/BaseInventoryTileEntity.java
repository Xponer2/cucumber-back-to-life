package com.blakebr0.cucumber.tileentity;

import javax.annotation.Nullable;

import io.github.xponer2.translator.registry.BlockEntityType;
import io.github.xponer2.translator.registry.HolderLookup;
import com.blakebr0.cucumber.inventory.BaseItemStackHandler;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public abstract class BaseInventoryTileEntity extends BaseTileEntity {
    public BaseInventoryTileEntity(BlockEntityType<?> type, BlockPos pos, @Nullable IBlockState state) {
        super(type, pos, state);
    }

    public abstract BaseItemStackHandler getInventory();

    // the inventory is stored in the tile entity's own tag ("Items", "Size"), like on 1.21
    @Override
    protected void loadAdditional(NBTTagCompound tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        this.getInventory().deserializeNBT(tag);
    }

    @Override
    protected void saveAdditional(NBTTagCompound tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        tag.merge(this.getInventory().serializeNBT());
    }

    // 1.21 Clearable
    public void clearContent() {
        this.getInventory().getStacks().clear();
        this.clearAdditional();
    }

    protected void clearAdditional() { }

    public boolean isUsableByPlayer(EntityPlayer player) {
        var pos = this.getPos();
        return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }
}
