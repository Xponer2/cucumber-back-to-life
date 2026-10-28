package com.blakebr0.cucumber.config;

import com.blakebr0.cucumber.Cucumber;
import com.blakebr0.cucumber.util.FeatureFlag;
import com.blakebr0.cucumber.util.FeatureFlags;

@FeatureFlags
public final class ModFeatureFlags {
    public static final FeatureFlag DATA_COMPONENT_TOOLTIPS = FeatureFlag.create(Cucumber.resource("data_component_tooltips"), ModConfigs.ENABLE_DATA_COMPONENT_TOOLTIPS);
    public static final FeatureFlag TAG_TOOLTIPS = FeatureFlag.create(Cucumber.resource("tag_tooltips"), ModConfigs.ENABLE_TAG_TOOLTIPS);
}
