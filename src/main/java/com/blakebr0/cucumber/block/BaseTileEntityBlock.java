package com.blakebr0.cucumber.block;

import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.block.BlockEntityTicker;
import io.github.xponer2.translator.block.BlockProperties;
import io.github.xponer2.translator.block.EntityBlock;
import io.github.xponer2.translator.registry.BlockEntityType;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A block with a tile entity: the 1.21 {@code EntityBlock} methods answer the 1.12.2 tile entity methods. The
 * ticker is asked for by the tile entity (Cucumber's BaseTileEntity).
 */
public abstract class BaseTileEntityBlock extends BaseBlock implements EntityBlock {
    public BaseTileEntityBlock(Function<BlockProperties, BlockProperties> properties) {
        super(properties);
    }

    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance) {
        super(sound, hardness, resistance);
    }

    public BaseTileEntityBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(sound, hardness, resistance, tool);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        // the world sets the position when it adds the tile entity
        return this.newBlockEntity(BlockPos.ORIGIN, state);
    }

    @Override
    public <T extends TileEntity> BlockEntityTicker<T> getTicker(World level, IBlockState state, BlockEntityType<T> type) {
        return level.isRemote
                ? this.getClientTicker(level, state, type)
                : this.getServerTicker(level, state, type);
    }

    protected <T extends TileEntity> BlockEntityTicker<T> getClientTicker(World level, IBlockState state, BlockEntityType<T> type) {
        return null;
    }

    protected <T extends TileEntity> BlockEntityTicker<T> getServerTicker(World level, IBlockState state, BlockEntityType<T> type) {
        return null;
    }

    @SuppressWarnings("unchecked")
    protected static <E extends TileEntity, A extends TileEntity> BlockEntityTicker<A> createTicker(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
        return typeA == typeB ? (BlockEntityTicker<A>) ticker : null;
    }
}
