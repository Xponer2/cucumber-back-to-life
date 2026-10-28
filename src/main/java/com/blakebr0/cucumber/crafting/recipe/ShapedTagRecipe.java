package com.blakebr0.cucumber.crafting.recipe;

import com.blakebr0.cucumber.crafting.OutputResolver;
import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xponer2.translator.recipe.CraftingBookCategory;
import io.github.xponer2.translator.recipe.RecipeSerializer;
import io.github.xponer2.translator.recipe.ShapedRecipe;
import io.github.xponer2.translator.recipe.ShapedRecipePattern;
import io.github.xponer2.translator.registry.HolderLookup;

import net.minecraft.item.ItemStack;

import io.github.xponer2.translator.network.RegistryFriendlyByteBuf;
import io.github.xponer2.translator.network.ByteBufCodecs;
import io.github.xponer2.translator.network.StreamCodec;

public class ShapedTagRecipe extends ShapedNoMirrorRecipe {
    private final OutputResolver outputResolver;
    private ItemStack result;

    public ShapedTagRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, OutputResolver outputResolver, boolean showNotification) {
        super(group, category, pattern, ItemStack.EMPTY, showNotification);
        this.outputResolver = outputResolver;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider lookup) {
        if (this.result == null) {
            this.result = this.outputResolver.resolve();
        }

        return this.result;
    }

    @Override
    public boolean isSpecial() {
        if (this.result == null) {
            this.result = this.outputResolver.resolve();
        }

        return this.result.isEmpty();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SHAPED_TAG.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedTagRecipe> {
        public static final MapCodec<ShapedTagRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                        OutputResolver.Tag.CODEC.fieldOf("result").forGetter(recipe -> (OutputResolver.Tag) recipe.outputResolver),
                        Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(ShapedRecipe::showNotification)
                ).apply(builder, ShapedTagRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedTagRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedTagRecipe.Serializer::toNetwork, ShapedTagRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapedTagRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedTagRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedTagRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var group = buffer.readUtf();
            var category = buffer.readEnumValue(CraftingBookCategory.class);
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = OutputResolver.create(buffer);
            var showNotification = buffer.readBoolean();

            return new ShapedTagRecipe(group, category, pattern, result, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedTagRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnumValue(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ByteBufCodecs.OPTIONAL_ITEM_STACK.encode(buffer, recipe.outputResolver.resolve());
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
