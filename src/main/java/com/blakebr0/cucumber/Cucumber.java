package com.blakebr0.cucumber;

import io.github.xponer2.translator.Translator;
import com.blakebr0.cucumber.command.ModCommands;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import com.blakebr0.cucumber.util.FeatureFlagInitializer;
import com.blakebr0.cucumber.client.handler.BowFOVHandler;
import com.blakebr0.cucumber.client.handler.DataComponentTooltipHandler;
import com.blakebr0.cucumber.client.handler.TagTooltipHandler;
import com.blakebr0.cucumber.crafting.TagMapper;
import com.blakebr0.cucumber.event.ItemBreakEventHandler;
import io.github.xponer2.translator.recipe.RecipeLoader;
import com.blakebr0.cucumber.config.ModConfigs;
import com.blakebr0.cucumber.init.ModConditionSerializers;
import com.blakebr0.cucumber.init.ModDataComponentTypes;
import com.blakebr0.cucumber.init.ModIngredientTypes;
import com.blakebr0.cucumber.init.ModRecipeSerializers;
import com.blakebr0.cucumber.init.ModSounds;
import com.blakebr0.cucumber.helper.RecipeHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Cucumber.MOD_ID, name = CucumberBuild.MOD_NAME, version = CucumberBuild.VERSION,
		dependencies = "required-after:" + Translator.MOD_ID + "@[" + Translator.VERSION + ",)", acceptedMinecraftVersions = "[1.12.2]")
public final class Cucumber {
	public static final String NAME = "Cucumber Library";
	public static final String MOD_ID = "cucumber";
	// constant, so the mods built on Cucumber can require this version in their @Mod
	public static final String VERSION = CucumberBuild.VERSION;
	public static final Logger LOGGER = LogManager.getLogger(NAME);

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// before the 1.12.2 registry events (after the pre-init of all mods)
		ModDataComponentTypes.REGISTRY.register(null);
		ModSounds.REGISTRY.register(null);
		ModConditionSerializers.REGISTRY.register(null);
		ModIngredientTypes.REGISTRY.register(null);
		ModRecipeSerializers.REGISTRY.register(null);
		ModConfigs.CLIENT.register("cucumber-client.cfg");
		FeatureFlagInitializer.init(event.getAsmData());
		ModConfigs.COMMON.register("cucumber-common.cfg");
		MinecraftForge.EVENT_BUS.register(new ItemBreakEventHandler());
		// 1.21 mixins: tag outputs of recipes (cucumber-tags.json), RecipeManagerLoadingEvent/RecipeManagerLoadedEvent
		RecipeLoader.onTagsLoaded(TagMapper::reloadTagMappings);
		RecipeLoader.addRecipeSource(RecipeHelper::fireRecipeManagerLoadingEvent);
		RecipeLoader.onLoaded(RecipeHelper::fireRecipeManagerLoadedEvent);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new TagMapper());
		if (event.getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(new BowFOVHandler());
			MinecraftForge.EVENT_BUS.register(new TagTooltipHandler());
			MinecraftForge.EVENT_BUS.register(new DataComponentTooltipHandler());
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new ModCommands());
	}

	public static ResourceLocation resource(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
