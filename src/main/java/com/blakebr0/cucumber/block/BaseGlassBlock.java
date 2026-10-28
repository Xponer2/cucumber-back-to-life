package com.blakebr0.cucumber.block;

import java.util.function.Function;

import io.github.xponer2.translator.block.BlockProperties;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 1.21 TransparentBlock: glass that hides the faces between two blocks of the same kind.
 */
public class BaseGlassBlock extends BaseBlock {
    public BaseGlassBlock(Function<BlockProperties, BlockProperties> properties) {
        super(glass(properties.apply(BlockProperties.of())));
    }

    public BaseGlassBlock(SoundType sound, float hardness, float resistance) {
        super(glass(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
        ));
    }

    public BaseGlassBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        super(glass(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
        ));
    }

    private static BlockProperties glass(BlockProperties properties) {
        return properties
                .noOcclusion()
                .isValidSpawn(BaseGlassBlock::never)
                .isRedstoneConductor(BaseGlassBlock::never)
                .isSuffocating(BaseGlassBlock::never)
                .isViewBlocking(BaseGlassBlock::never);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        var other = world.getBlockState(pos.offset(side));
        return other.getBlock() != this && super.shouldSideBeRendered(state, world, pos, side);
    }

    private static boolean never(IBlockState state, IBlockAccess level, BlockPos pos, Object entity) {
        return false;
    }

    private static boolean never(IBlockState state, IBlockAccess level, BlockPos pos) {
        return false;
    }
}
