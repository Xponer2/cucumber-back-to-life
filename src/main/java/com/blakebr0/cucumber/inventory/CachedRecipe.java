package com.blakebr0.cucumber.inventory;

import io.github.xponer2.translator.recipe.Recipe;
import io.github.xponer2.translator.recipe.RecipeHolder;
import io.github.xponer2.translator.recipe.RecipeInput;
import io.github.xponer2.translator.recipe.RecipeManager;
import io.github.xponer2.translator.recipe.RecipeType;

import net.minecraft.world.World;

public class CachedRecipe<I extends RecipeInput, T extends Recipe<I>> {
    private final RecipeType<T> type;
    private T recipe;

    public CachedRecipe(RecipeType<T> type) {
        this.type = type;
    }

    /**
     * Checks if the provided inventory matches the cached recipe. If it doesn't match, it will try to find a new recipe
     * @param inventory the recipe inventory
     * @param level the level
     * @return does this recipe match the inventory
     */
    public boolean check(I inventory, World level) {
        if (this.recipe != null && this.recipe.matches(inventory, level))
            return true;

        this.recipe = RecipeManager.get()
                .getRecipeFor(this.type, inventory, level)
                .map(RecipeHolder::value)
                .orElse(null);

        return this.recipe != null;
    }

    public boolean exists() {
        return this.recipe != null;
    }

    public T get() {
        return this.recipe;
    }

    public T checkAndGet(I inventory, World level) {
        if (this.check(inventory, level))
            return this.recipe;

        return null;
    }
}
