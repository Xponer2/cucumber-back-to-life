package com.blakebr0.cucumber.block;

import java.util.function.Function;
import java.util.function.Supplier;
import io.github.xponer2.translator.block.BlockProperties;
import io.github.xponer2.translator.block.WallBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;

public class BaseWallBlock extends WallBlock {
    public BaseWallBlock(Supplier<Block> block) {
        this(BlockProperties.ofFullCopy(block.get()));
    }

    public BaseWallBlock(BlockProperties properties) {
        super(properties);
    }

    public BaseWallBlock(Function<BlockProperties, BlockProperties> properties) {
        this(properties.apply(BlockProperties.of()));
    }

    public BaseWallBlock(SoundType sound, float hardness, float resistance) {
        this(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
        );
    }

    public BaseWallBlock(SoundType sound, float hardness, float resistance, boolean tool) {
        this(BlockProperties.of()
                .sound(sound)
                .strength(hardness, resistance)
                .requiresCorrectToolForDrops()
        );
    }
}
