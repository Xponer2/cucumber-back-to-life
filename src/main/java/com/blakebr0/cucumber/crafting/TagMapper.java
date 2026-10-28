package com.blakebr0.cucumber.crafting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.blakebr0.cucumber.Cucumber;
import com.blakebr0.cucumber.compat.almostunified.AlmostUnifiedAdapter;
import com.blakebr0.cucumber.config.ModConfigs;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.io.IOUtils;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import io.github.xponer2.translator.tags.TagManager;
import io.github.xponer2.translator.util.BuiltInRegistries;

public class TagMapper {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, String> TAG_TO_ITEM_MAP = new HashMap<>();

    public static void reloadTagMappings() {
        var stopwatch = Stopwatch.createStarted();
        var dir = Loader.instance().getConfigDir().toPath().toFile();

        TAG_TO_ITEM_MAP.clear();

        if (dir.exists() && dir.isDirectory()) {
            var file = Loader.instance().getConfigDir().toPath().resolve("cucumber-tags.json").toFile();

            if (file.exists() && file.isFile()) {
                JsonObject json;
                FileReader reader = null;

                try {
                    reader = new FileReader(file);
                    json = new JsonParser().parse(reader).getAsJsonObject();

                    json.entrySet().stream().filter(e -> {
                        var value = e.getValue().getAsString();
                        return !"__comment".equalsIgnoreCase(e.getKey()) && !value.isEmpty() && !"null".equalsIgnoreCase(value);
                    }).forEach(entry -> {
                        var tagId = entry.getKey();
                        var itemId = entry.getValue().getAsString();

                        TAG_TO_ITEM_MAP.put(tagId, itemId);

                        // if auto refresh tag entries is enabled, we check any entries that contain an item ID to see
                        // if they are still present. if not we just refresh the entry
                        if (ModConfigs.AUTO_REFRESH_TAG_ENTRIES.get()) {
                            if (!itemId.isEmpty() && !"null".equalsIgnoreCase(itemId)) {
                                var item = BuiltInRegistries.ITEM.get(io.github.xponer2.translator.util.ResourceLocations.parse(itemId));
                                if (item == null || item == Items.AIR) {
                                    addTagToFile(tagId, json, file, false);
                                }
                            }
                        }
                    });

                    // save changes to disk if refresh is enabled
                    if (ModConfigs.AUTO_REFRESH_TAG_ENTRIES.get())
                        saveToFile(json, file);

                    reader.close();
                } catch (Exception e) {
                    Cucumber.LOGGER.error("An error occurred while reading cucumber-tags.json", e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            } else {
                generateNewConfig(file);
            }
        }

        stopwatch.stop();

        Cucumber.LOGGER.info("Loaded cucumber-tags.json in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public static Item getItemForTag(String tagId) {
        var preferredItem = AlmostUnifiedAdapter.getPreferredItemForTag(tagId);
        if (preferredItem != null) {
            return preferredItem;
        }

        if (TAG_TO_ITEM_MAP.containsKey(tagId)) {
            var id = TAG_TO_ITEM_MAP.get(tagId);
            return BuiltInRegistries.ITEM.get(io.github.xponer2.translator.util.ResourceLocations.parse(id));
        } else {
            var file = Loader.instance().getConfigDir().toPath().resolve("cucumber-tags.json").toFile();
            if (!file.exists()) {
                generateNewConfig(file);
            }

            if (file.isFile()) {
                JsonObject json = null;
                FileReader reader = null;

                try {
                    reader = new FileReader(file);
                    json = new JsonParser().parse(reader).getAsJsonObject();
                } catch (Exception e) {
                    Cucumber.LOGGER.error("An error occurred while reading cucumber-tags.json", e);
                } finally {
                    IOUtils.closeQuietly(reader);
                }

                if (json != null) {
                    if (json.has(tagId)) {
                        var itemId = json.get(tagId).getAsString();
                        if (itemId.isEmpty() || "null".equalsIgnoreCase(itemId))
                            return addTagToFile(tagId, json, file);

                        TAG_TO_ITEM_MAP.put(tagId, itemId);

                        return BuiltInRegistries.ITEM.get(io.github.xponer2.translator.util.ResourceLocations.parse(itemId));
                    }

                    return addTagToFile(tagId, json, file);
                }
            }

            return Items.AIR;
        }
    }

    public static ItemStack getItemStackForTag(String tagId, int size) {
        var item = getItemForTag(tagId);
        return item != null && item != Items.AIR ? new ItemStack(item, size) : ItemStack.EMPTY;
    }

    private static Item addTagToFile(String tagId, JsonObject json, File file) {
        return addTagToFile(tagId, json, file, true);
    }

    private static Item addTagToFile(String tagId, JsonObject json, File file, boolean save) {
        var mods = ModConfigs.MOD_TAG_PRIORITIES.get();
        // the tag's item from the mod that comes first in the priority list (mods not in the list last)
        var item = TagManager.itemTag(io.github.xponer2.translator.util.ResourceLocations.parse(tagId)).stream().map(ItemStack::getItem).min((item1, item2) -> {
            var index1 = mods.indexOf(BuiltInRegistries.ITEM.getKey(item1).getNamespace());
            var index2 = mods.indexOf(BuiltInRegistries.ITEM.getKey(item2).getNamespace());
            return Integer.compare(index1 == -1 ? Integer.MAX_VALUE : index1, index2 == -1 ? Integer.MAX_VALUE : index2);
        }).orElse(Items.AIR);

        var itemId = "null";
        if (item != Items.AIR && BuiltInRegistries.ITEM.containsValue(item)) {
            itemId = BuiltInRegistries.ITEM.getKey(item).toString();
        }

        json.addProperty(tagId, itemId);
        TAG_TO_ITEM_MAP.put(tagId, itemId);

        if (save) {
            saveToFile(json, file);
        }

        return item;
    }

    private static void saveToFile(JsonObject json, File file) {
        try (var writer = new FileWriter(file)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            Cucumber.LOGGER.error("An error occurred while writing to cucumber-tags.json", e);
        }
    }

    private static void generateNewConfig(File file) {
        try (var writer = new FileWriter(file)) {
            var object = new JsonObject();
            object.addProperty("__comment", "Instructions: https://blakesmods.com/docs/cucumber/tags-config");

            GSON.toJson(object, writer);
        } catch (IOException e) {
            Cucumber.LOGGER.error("An error occurred while creating cucumber-tags.json", e);
        }
    }
}
