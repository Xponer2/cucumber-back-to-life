package com.blakebr0.cucumber.tileentity;

import javax.annotation.Nullable;

import io.github.xponer2.translator.block.BlockEntityTicker;
import io.github.xponer2.translator.block.EntityBlock;
import io.github.xponer2.translator.registry.BlockEntityType;
import io.github.xponer2.translator.registry.HolderLookup;
import io.github.xponer2.translator.registry.RegistryAccess;
import com.blakebr0.cucumber.helper.TileEntityHelper;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The 1.21 block entity on 1.12.2: subclasses keep the 1.21 constructor {@code (pos, state)} (plus the no-arg one
 * 1.12.2 needs to load them) and the 1.21 {@code loadAdditional}/{@code saveAdditional}; this class maps the 1.12.2
 * NBT, sync and tick methods to them. Ticking: the ticker of the block ({@code EntityBlock#getTicker}).
 */
public class BaseTileEntity extends TileEntity implements ITickable {
	private final BlockEntityType<?> type;
	private boolean isChanged = false;
	private BlockEntityTicker<TileEntity> ticker;
	private boolean tickerResolved;

	public BaseTileEntity(BlockEntityType<?> type, BlockPos pos, @Nullable IBlockState state) {
		this.type = type;
		this.pos = pos.toImmutable();
	}

	public BlockEntityType<?> getType() {
		return this.type;
	}

	public IBlockState getBlockState() {
		return this.world != null ? this.world.getBlockState(this.pos) : this.getBlockType().getDefaultState();
	}

	// 1.21 save/load

	protected void loadAdditional(NBTTagCompound tag, HolderLookup.Provider registries) { }

	protected void saveAdditional(NBTTagCompound tag, HolderLookup.Provider registries) { }

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		this.loadAdditional(tag, RegistryAccess.INSTANCE);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		this.saveAdditional(tag, RegistryAccess.INSTANCE);
		return tag;
	}

	// client sync: the whole tile entity, like 1.21 saveWithFullMetadata

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
		this.readFromNBT(packet.getNbtCompound());
	}

	// a state change of the same block (facing, running) keeps the tile entity, like on 1.21
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update() {
		if (!this.tickerResolved) {
			this.tickerResolved = true;
			var state = this.getBlockState();
			if (state.getBlock() instanceof EntityBlock block) {
				this.ticker = (BlockEntityTicker<TileEntity>) block.getTicker(this.world, state, (BlockEntityType<TileEntity>) this.type);
			}
		}
		if (this.ticker != null) {
			this.ticker.tick(this.world, this.pos, this.getBlockState(), this);
		}
	}

	// 1.21 setChanged
	public void setChanged() {
		this.markDirty();
	}

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
