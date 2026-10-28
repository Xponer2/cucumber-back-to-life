package com.blakebr0.cucumber.util;

import com.blakebr0.cucumber.Cucumber;

import net.minecraftforge.fml.common.discovery.ASMDataTable;

public final class FeatureFlagInitializer {
    // loads the classes annotated with @FeatureFlags, found in the annotation data of the mods (pre-init)
    public static void init(ASMDataTable data) {
        for (var annotation : data.getAll(FeatureFlags.class.getName())) {
            try {
                Class.forName(annotation.getClassName(), true, FeatureFlagInitializer.class.getClassLoader());
            } catch (Exception e) {
                Cucumber.LOGGER.error("Error loading feature flags: {}", annotation.getClassName(), e);
            }
        }
    }
}
