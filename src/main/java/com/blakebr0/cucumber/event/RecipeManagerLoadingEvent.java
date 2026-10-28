package com.blakebr0.cucumber.event;

import java.util.List;

import io.github.xponer2.translator.recipe.RecipeHolder;
import io.github.xponer2.translator.recipe.RecipeManager;

import net.minecraftforge.fml.common.eventhandler.Event;

public class RecipeManagerLoadingEvent extends Event {
    private final RecipeManager manager;
    private final List<RecipeHolder<?>> recipes;

    public RecipeManagerLoadingEvent(RecipeManager manager, List<RecipeHolder<?>> recipes) {
        this.manager = manager;
        this.recipes = recipes;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }

    public void addRecipe(RecipeHolder<?> recipe) {
        this.recipes.add(recipe);
    }
}
