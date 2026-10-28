package com.blakebr0.cucumber.block;

import java.util.function.Function;
import io.github.xponer2.translator.block.BlockProperties;
import io.github.xponer2.translator.block.TransparentBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BaseGlassBlock extends TransparentBlock {
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

    private static boolean never(IBlockState state, IBlockAccess level, BlockPos pos, Object entity) {
        return false;
    }

    private static boolean never(IBlockState state, IBlockAccess level, BlockPos pos) {
        return false;
    }
}
