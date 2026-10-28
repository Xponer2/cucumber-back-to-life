package com.blakebr0.cucumber.block;

import java.util.function.Function;
import java.util.function.Supplier;
import io.github.xponer2.translator.block.BlockProperties;
import io.github.xponer2.translator.block.SlabBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;

public class BaseSlabBlock extends SlabBlock {
    public BaseSlabBlock(Supplier<Block> block) {
        this(BlockProperties.ofFullCopy(block.get()));
    }

    public BaseSlabBlock(BlockProperties properties) {
        super(properties);
    }

    public BaseSlabBlock(Function<BlockProperties, BlockProperties> properties) {
        this(properties.apply(BlockProperties.of()));
    }

    public BaseSlabBlock(SoundType sound, float hardness, float resistance) {
        this(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
        );
    }

    public BaseSlabBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        this(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
        );
    }
}
