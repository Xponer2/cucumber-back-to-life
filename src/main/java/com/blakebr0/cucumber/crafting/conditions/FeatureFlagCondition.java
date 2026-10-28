package com.blakebr0.cucumber.crafting.conditions;

import com.blakebr0.cucumber.util.FeatureFlag;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xponer2.translator.recipe.ICondition;
import io.github.xponer2.translator.util.Codecs;

import net.minecraft.util.ResourceLocation;

import io.github.xponer2.translator.util.Codecs;

public final class FeatureFlagCondition implements ICondition {
    private final ResourceLocation flag;

    public FeatureFlagCondition(ResourceLocation flag) {
        this.flag = flag;
    }

    public ResourceLocation flag() {
        return flag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FeatureFlagCondition)) return false;
        FeatureFlagCondition that = (FeatureFlagCondition) o;
        return java.util.Objects.equals(flag, that.flag);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(flag);
    }

    @Override
    public String toString() {
        return "FeatureFlagCondition[" + "flag=" + flag + "]";
    }

    public static final MapCodec<FeatureFlagCondition> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codecs.RESOURCE_LOCATION.fieldOf("flag").forGetter(FeatureFlagCondition::flag)
            ).apply(builder, FeatureFlagCondition::new)
    );

    @Override
    public boolean test(IContext context) {
        var flag = FeatureFlag.from(this.flag);
        return flag.isEnabled();
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
