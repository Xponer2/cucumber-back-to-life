package com.blakebr0.cucumber.lib;

import com.blakebr0.cucumber.Cucumber;

import net.minecraft.block.Block;

import io.github.xponer2.translator.util.TagKey;
import io.github.xponer2.translator.tags.BlockTags;

public final class ModTags {
    public static final TagKey<Block> MINEABLE_WITH_PAXEL = BlockTags.create(Cucumber.resource("mineable/paxel"));
    public static final TagKey<Block> MINEABLE_WITH_SICKLE = BlockTags.create(Cucumber.resource("mineable/sickle"));
}
