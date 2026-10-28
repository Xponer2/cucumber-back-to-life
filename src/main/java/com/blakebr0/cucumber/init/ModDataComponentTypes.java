package com.blakebr0.cucumber.init;

import com.blakebr0.cucumber.Cucumber;
import com.mojang.serialization.Codec;
import io.github.xponer2.translator.registry.DeferredHolder;
import io.github.xponer2.translator.registry.DeferredRegister;

import io.github.xponer2.translator.component.DataComponentType;
import io.github.xponer2.translator.util.Registries;
import io.github.xponer2.translator.network.ByteBufCodecs;

public final class ModDataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, Cucumber.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WATERING_CAN_FILLED = REGISTRY.register("watering_can_filled", () -> DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
}
