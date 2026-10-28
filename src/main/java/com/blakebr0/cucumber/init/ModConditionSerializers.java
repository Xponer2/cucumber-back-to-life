package com.blakebr0.cucumber.init;

import com.blakebr0.cucumber.Cucumber;
import com.blakebr0.cucumber.crafting.conditions.FeatureFlagCondition;
import com.mojang.serialization.MapCodec;
import io.github.xponer2.translator.recipe.ICondition;
import io.github.xponer2.translator.registry.DeferredHolder;
import io.github.xponer2.translator.registry.DeferredRegister;
import io.github.xponer2.translator.registry.NeoForgeRegistries;

public final class ModConditionSerializers {
    public static final DeferredRegister<MapCodec<? extends ICondition>> REGISTRY = DeferredRegister.create(NeoForgeRegistries.CONDITION_SERIALIZERS, Cucumber.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<? extends ICondition>> FEATURE_FLAG = REGISTRY.register("feature_flag", () -> FeatureFlagCondition.CODEC);
}
