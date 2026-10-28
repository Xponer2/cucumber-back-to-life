package com.blakebr0.cucumber.event;

import io.github.xponer2.translator.recipe.RecipeManager;

import net.minecraftforge.fml.common.eventhandler.Event;

public class RecipeManagerLoadedEvent extends Event {
    private final RecipeManager manager;

    public RecipeManagerLoadedEvent(RecipeManager manager) {
        this.manager = manager;
    }

    public RecipeManager getRecipeManager() {
        return this.manager;
    }
}
