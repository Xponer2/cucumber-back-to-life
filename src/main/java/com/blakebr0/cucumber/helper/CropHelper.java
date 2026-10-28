package com.blakebr0.cucumber.helper;

import java.lang.reflect.Method;
import java.util.HashSet;

import com.blakebr0.cucumber.Cucumber;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public final class CropHelper {
    private static final HashSet<Block> ERRORED_BLOCKS = new HashSet<>();
    private static final Method GET_SEED;

    static {
        // BlockCrops#getSeed (protected), by its SRG name
        GET_SEED = ObfuscationReflectionHelper.findMethod(BlockCrops.class, "func_149866_i", Item.class);
    }

    public static Item getSeedsItem(Block block) {
        try {
            return (Item) GET_SEED.invoke(block);
        } catch (Exception e) {
            if (ERRORED_BLOCKS.add(block)) {
                Cucumber.LOGGER.error("Unable to get seed from crop {}", e.getLocalizedMessage());
            }
        }

        return null;
    }
}
