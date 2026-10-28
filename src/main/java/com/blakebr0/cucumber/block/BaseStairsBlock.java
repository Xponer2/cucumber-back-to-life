package com.blakebr0.cucumber.block;

import java.util.function.Function;

import io.github.xponer2.translator.block.BlockProperties;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;

/**
 * 1.12.2 stairs take material, hardness, resistance and sound from the model block; given properties override
 * hardness, resistance, light and sound.
 */
public class BaseStairsBlock extends BlockStairs {
    public BaseStairsBlock(IBlockState state) {
        super(state);
        this.useNeighborBrightness = true;
    }

    public BaseStairsBlock(IBlockState state, BlockProperties properties) {
        super(state);
        properties.applyTo(this, this::setSoundType);
        this.useNeighborBrightness = true;
    }

    public BaseStairsBlock(IBlockState state, Function<BlockProperties, BlockProperties> properties) {
        this(state, properties.apply(BlockProperties.of()));
    }

    public BaseStairsBlock(IBlockState state, SoundType sound, float hardness, float resistance) {
        this(state, BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
        );
    }

    public BaseStairsBlock(IBlockState state, SoundType sound, float hardness, float resistance, boolean tool) {
        this(state, BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
        );
    }
}
