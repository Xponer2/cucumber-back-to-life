package com.blakebr0.cucumber.tileentity;

import javax.annotation.Nullable;
import io.github.xponer2.translator.block.BlockEntity;
import io.github.xponer2.translator.registry.BlockEntityType;
import com.blakebr0.cucumber.helper.TileEntityHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;

public class BaseTileEntity extends BlockEntity {
	private boolean isChanged = false;

	public BaseTileEntity(BlockEntityType<?> type, BlockPos pos, @Nullable IBlockState state) {
		super(type, pos, state);
	}

	// the whole block entity, like 1.21 saveWithFullMetadata
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	// 1.21 setChanged (1.12.2 code, like the inventories, calls markDirty)
	@Override
	public void markDirty() {
		super.markDirty();
		this.isChanged = true;
	}

	public void setChangedFast() {
		if (this.world != null) {
			this.world.markChunkDirty(this.getPos(), this);
			this.isChanged = true;
		}
	}

	public void setChangedAndDispatch() {
		this.markDirty();
		TileEntityHelper.dispatchToNearbyPlayers(this);
		this.isChanged = false;
	}

	public void dispatchIfChanged() {
		if (this.isChanged) {
			TileEntityHelper.dispatchToNearbyPlayers(this);
			this.isChanged = false;
		}
	}
}
