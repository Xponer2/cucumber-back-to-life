package com.blakebr0.cucumber.crafting.recipe;

import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.xponer2.translator.recipe.CraftingBookCategory;
import io.github.xponer2.translator.recipe.CraftingInput;
import io.github.xponer2.translator.recipe.RecipeSerializer;
import io.github.xponer2.translator.recipe.ShapedRecipe;
import io.github.xponer2.translator.recipe.ShapedRecipePattern;
import io.github.xponer2.translator.util.Codecs;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.world.World;

import io.github.xponer2.translator.network.RegistryFriendlyByteBuf;
import io.github.xponer2.translator.network.ByteBufCodecs;
import io.github.xponer2.translator.network.StreamCodec;
import io.github.xponer2.translator.util.Codecs;

public class ShapedNoMirrorRecipe extends ShapedRecipe {
    private final ItemStack result;

    public ShapedNoMirrorRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack result, boolean showNotification) {
        super(group, category, pattern, result, showNotification);
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput inventory, World level) {
        for (int i = 0; i <= inventory.width() - this.pattern.width(); i++) {
            for (int j = 0; j <= inventory.height() - this.pattern.height(); j++) {
                if (this.checkMatch(inventory, i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.CRAFTING_SHAPED_NO_MIRROR.get();
    }

    private boolean checkMatch(CraftingInput inventory, int x, int y) {
        for (var i = 0; i < inventory.width(); i++) {
            for (var j = 0; j < inventory.height(); j++) {
                var k = i - x;
                var l = j - y;
                var ingredient = Ingredient.EMPTY;

                if (k >= 0 && l >= 0 && k < this.pattern.width() && l < this.pattern.height()) {
                    ingredient = this.getIngredients().get(k + l * this.pattern.width());
                }

                if (!ingredient.test(inventory.getItem(i + j * inventory.width()))) {
                    return false;
                }
            }
        }

        return true;
    }

    public static class Serializer implements RecipeSerializer<ShapedNoMirrorRecipe> {
        public static final MapCodec<ShapedNoMirrorRecipe> CODEC = RecordCodecBuilder.mapCodec(builder ->
                builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                        CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                        ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                        Codecs.ITEM_STACK.fieldOf("result").forGetter(recipe -> recipe.result),
                        Codec.BOOL.optionalFieldOf("show_notification", Boolean.TRUE).forGetter(ShapedRecipe::showNotification)
                ).apply(builder, ShapedNoMirrorRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedNoMirrorRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedNoMirrorRecipe.Serializer::toNetwork, ShapedNoMirrorRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<ShapedNoMirrorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedNoMirrorRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static ShapedNoMirrorRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            var group = buffer.readUtf();
            var category = buffer.readEnumValue(CraftingBookCategory.class);
            var pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            var result = ByteBufCodecs.ITEM_STACK.decode(buffer);
            var showNotification = buffer.readBoolean();

            return new ShapedNoMirrorRecipe(group, category, pattern, result, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedNoMirrorRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnumValue(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ByteBufCodecs.ITEM_STACK.encode(buffer, recipe.result);
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
