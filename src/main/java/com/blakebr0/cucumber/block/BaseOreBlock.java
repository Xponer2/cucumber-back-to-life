package com.blakebr0.cucumber.block;

import java.util.function.Function;

import io.github.xponer2.translator.block.BlockProperties;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BaseOreBlock extends BaseBlock {
    private final int minExp;
    private final int maxExp;

    public BaseOreBlock(Function<BlockProperties, BlockProperties> properties, int minExp, int maxExp) {
        super(properties.compose(BlockProperties::requiresCorrectToolForDrops));
        this.minExp = minExp;
        this.maxExp = maxExp;
    }

    public BaseOreBlock(SoundType sound, float hardness, float resistance, int minExp, int maxExp) {
        this(p -> p.sound(sound).strength(hardness, resistance), minExp, maxExp);
    }

    // 1.12.2 Forge only asks for the experience when the block isn't mined with silk touch
    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        return this.minExp + RANDOM.nextInt(this.maxExp - this.minExp + 1);
    }
}
