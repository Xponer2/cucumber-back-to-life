package com.blakebr0.cucumber.init;

import com.blakebr0.cucumber.Cucumber;
import io.github.xponer2.translator.registry.DeferredHolder;
import io.github.xponer2.translator.registry.DeferredRegister;

import net.minecraft.util.SoundEvent;

import io.github.xponer2.translator.util.BuiltInRegistries;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Cucumber.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> WATERING_CAN = REGISTRY.register("watering_can", () -> new SoundEvent(Cucumber.resource("watering_can")));
}
