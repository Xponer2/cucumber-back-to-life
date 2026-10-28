package com.blakebr0.cucumber.crafting.recipe;

import com.blakebr0.cucumber.crafting.OutputResolver;
import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xponer2.translator.network.ByteBufCodecs;
import io.github.xponer2.translator.recipe.CraftingBookCategory;
import io.github.xponer2.translator.recipe.RecipeSerializer;
import io.github.xponer2.translator.recipe.ShapedRecipePattern;
import io.github.xponer2.translator.recipe.ShapelessRecipe;
import io.github.xponer2.translator.registry.HolderLookup;
import io.github.xponer2.translator.util.Codecs;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import io.github.xponer2.translator.network.RegistryFriendlyByteBuf;
import io.github.xponer2.translator.network.ByteBufCodecs;
import io.github.xponer2.translator.network.StreamCodec;

public class ShapelessTagRecipe extends ShapelessRecipe {
    private final OutputResolver outputResolver;
    private ItemStack result;

    public ShapelessTagRecipe(String group, CraftingBookCategory category, NonNullList<Ingredient> inputs, OutputResolver outputResolver) {
        super(group, category, ItemStack.EMPTY, inputs);
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
        return ModRecipeSerializers.CRAFTING_SHAPELESS_TAG.get();
    }

    public static class Serializer implements RecipeSerializer<ShapelessTagRecipe> {
        public static final MapCodec<ShapelessTagRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapelessRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapelessRecipe::category),
                        Codecs.INGREDIENT_NONEMPTY
                                .listOf()
                                .fieldOf("ingredients")
                                .flatXmap(
                                        field -> {
                                            Ingredient[] ingredients = field.toArray(new Ingredient[0]); // Neo skip the empty check and immediately create the array.
                                            if (ingredients.length == 0) {
                                                return DataResult.error("No ingredients for shapeless recipe");
                                            } else {
                                                return ingredients.length > ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()
                                                        ? DataResult.error(String.format("Too many ingredients for shapeless recipe. The maximum is: %s", ShapedRecipePattern.getMaxHeight() * ShapedRecipePattern.getMaxWidth()))
                                                        : DataResult.success(NonNullList.from(Ingredient.EMPTY, ingredients));
                                            }
                                        },
                                        DataResult::success
                                )
                                .forGetter(ShapelessRecipe::getIngredients),
                        OutputResolver.Tag.CODEC.fieldOf("result").forGetter(recipe -> (OutputResolver.Tag) recipe.outputResolver)
                ).apply(builder, ShapelessTagRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessTagRecipe> STREAM_CODEC = StreamCodec.of(
                ShapelessTagRecipe.Serializer::toNetwork, ShapelessTagRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapelessTagRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapelessTagRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapelessTagRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var group = buffer.readUtf(32767);
            var size = buffer.readVarInt();
            var category = buffer.readEnumValue(CraftingBookCategory.class);

            NonNullList<Ingredient> ingredients = NonNullList.withSize(size, Ingredient.EMPTY);
            ingredients.replaceAll(ingredient -> ByteBufCodecs.INGREDIENT.decode(buffer));

            var result = OutputResolver.create(buffer);

            return new ShapelessTagRecipe(group, category, ingredients, result);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapelessTagRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnumValue(recipe.category());

            for (var ingredient : recipe.getIngredients()) {
                ByteBufCodecs.INGREDIENT.encode(buffer, ingredient);
            }

            ByteBufCodecs.ITEM_STACK.encode(buffer, recipe.outputResolver.resolve());
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
