package com.blakebr0.cucumber.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.blakebr0.cucumber.Cucumber;
import io.github.xponer2.translator.recipe.Recipe;
import io.github.xponer2.translator.recipe.RecipeHolder;
import io.github.xponer2.translator.recipe.RecipeInput;
import io.github.xponer2.translator.recipe.RecipeManager;
import io.github.xponer2.translator.recipe.RecipeType;
import com.blakebr0.cucumber.event.RecipeManagerLoadedEvent;
import com.blakebr0.cucumber.event.RecipeManagerLoadingEvent;
import com.google.common.base.Stopwatch;

import net.minecraftforge.common.MinecraftForge;

/**
 * 1.12.2 has one recipe manager for the game ({@link RecipeManager#get()}); the 1.21 mixins that tracked the current
 * one and fired the loading events are replaced by the recipe loader calling the fire methods.
 */
public final class RecipeHelper {
    public static RecipeManager getRecipeManager() {
        return RecipeManager.get();
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> type) {
        return byType(getRecipeManager(), type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeManager manager, RecipeType<T> type) {
        return manager.getAllRecipesFor(type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> byTypeValues(RecipeType<T> type) {
        return byTypeValues(getRecipeManager(), type);
    }

    public static <I extends RecipeInput, T extends Recipe<I>> List<T> byTypeValues(RecipeManager manager, RecipeType<T> type) {
        return byType(manager, type).stream().map(RecipeHolder::value).collect(Collectors.toList());
    }

    public static Collection<RecipeHolder<?>> getAllRecipes() {
        return getRecipeManager().getRecipes();
    }

    /**
     * The recipes mods add in code ({@link RecipeManagerLoadingEvent}), while the data recipes are loaded.
     */
    public static List<RecipeHolder<?>> fireRecipeManagerLoadingEvent(RecipeManager manager) {
        var stopwatch = Stopwatch.createStarted();
        var recipes = new ArrayList<RecipeHolder<?>>();
        try {
            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadingEvent(manager, recipes));
        } catch (Exception e) {
            Cucumber.LOGGER.error("An error occurred while firing RecipeManagerLoadingEvent", e);
        }
        Cucumber.LOGGER.info("Registered {} recipes in {} ms", recipes.size(), stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
        return recipes;
    }

    public static void fireRecipeManagerLoadedEvent(RecipeManager manager) {
        try {
            MinecraftForge.EVENT_BUS.post(new RecipeManagerLoadedEvent(manager));
        } catch (Exception e) {
            Cucumber.LOGGER.error("An error occurred while firing RecipeManagerLoadedEvent", e);
        }
    }
}
