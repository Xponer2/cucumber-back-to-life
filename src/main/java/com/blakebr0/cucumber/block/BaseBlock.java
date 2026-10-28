package com.blakebr0.cucumber.block;

import java.util.function.Function;

import javax.annotation.Nullable;

import io.github.xponer2.translator.block.BlockProperties;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BaseBlock extends Block {
	protected final BlockProperties properties;

	public BaseBlock(Function<BlockProperties, BlockProperties> properties) {
		this(properties.apply(BlockProperties.of()));
	}

	public BaseBlock(BlockProperties properties) {
		super(properties.material(), properties.mapColor());
		this.properties = properties;
		properties.applyTo(this, this::setSoundType);
		// the Block constructor asked isOpaqueCube before the properties were set
		this.fullBlock = properties.canOcclude();
		this.lightOpacity = this.fullBlock ? 255 : 0;
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

	public BlockProperties getProperties() {
		return this.properties;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isOpaqueCube(IBlockState state) {
		return this.properties == null || this.properties.canOcclude();
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean isFullCube(IBlockState state) {
		return this.properties == null || this.properties.canOcclude();
	}

	@Nullable
	@Override
	@SuppressWarnings("deprecation")
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return this.properties.hasCollision() ? super.getCollisionBoundingBox(state, world, pos) : NULL_AABB;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean causesSuffocation(IBlockState state) {
		var suffocating = this.properties.isSuffocating(state, null, null);
		return suffocating != null ? suffocating : super.causesSuffocation(state);
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		var conductor = this.properties.isRedstoneConductor(state, world, pos);
		return conductor != null ? conductor : super.isNormalCube(state, world, pos);
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {
		return this.properties.isValidSpawn(state, world, pos, type) && super.canCreatureSpawn(state, world, pos, type);
	}
}
