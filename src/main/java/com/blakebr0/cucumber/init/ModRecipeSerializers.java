package com.blakebr0.cucumber.init;

import com.blakebr0.cucumber.Cucumber;
import com.blakebr0.cucumber.crafting.recipe.ShapedNoMirrorRecipe;
import com.blakebr0.cucumber.crafting.recipe.ShapedTagRecipe;
import com.blakebr0.cucumber.crafting.recipe.ShapedTransferComponentsRecipe;
import com.blakebr0.cucumber.crafting.recipe.ShapedTransferDamageRecipe;
import com.blakebr0.cucumber.crafting.recipe.ShapelessTagRecipe;
import io.github.xponer2.translator.recipe.RecipeSerializer;
import io.github.xponer2.translator.registry.DeferredHolder;
import io.github.xponer2.translator.registry.DeferredRegister;

import io.github.xponer2.translator.util.BuiltInRegistries;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Cucumber.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CRAFTING_SHAPED_NO_MIRROR = REGISTRY.register("shaped_no_mirror", ShapedNoMirrorRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CRAFTING_SHAPED_TRANSFER_DAMAGE = REGISTRY.register("shaped_transfer_damage", ShapedTransferDamageRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CRAFTING_SHAPED_TRANSFER_COMPONENTS = REGISTRY.register("shaped_transfer_components", ShapedTransferComponentsRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CRAFTING_SHAPED_TAG = REGISTRY.register("shaped_tag", ShapedTagRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> CRAFTING_SHAPELESS_TAG = REGISTRY.register("shapeless_tag", ShapelessTagRecipe.Serializer::new);
}
