package com.blakebr0.cucumber.block;

import java.util.function.Function;
import io.github.xponer2.translator.block.BlockProperties;
import io.github.xponer2.translator.block.PropertiesBlock;
import net.minecraft.block.SoundType;

public class BaseBlock extends PropertiesBlock {
	public BaseBlock(Function<BlockProperties, BlockProperties> properties) {
		this(properties.apply(BlockProperties.of()));
	}

	public BaseBlock(BlockProperties properties) {
		super(properties);
	}

	public BaseBlock(SoundType sound, float hardness, float resistance) {
		this(BlockProperties.of()
				.sound(sound)
				.strength(hardness, resistance)
		);
	}

	public BaseBlock(SoundType sound, float hardness, float resistance, boolean tool) {
		this(BlockProperties.of()
				.sound(sound)
				.strength(hardness, resistance)
				.requiresCorrectToolForDrops()
		);
	}
}
