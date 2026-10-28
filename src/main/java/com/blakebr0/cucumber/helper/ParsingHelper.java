package com.blakebr0.cucumber.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;

public final class ParsingHelper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static int parseHex(String s, String name) {
        // remove leading # if it exists
        if (s.startsWith("#")) {
            s = s.substring(1);
        }

        try {
            return Integer.parseInt(s, 16);
        } catch (NumberFormatException e) {
            throw new JsonSyntaxException("Invalid color provided for color " + name);
        }
    }

    public static NBTTagCompound parseNBT(JsonElement json) {
        try {
            if (json.isJsonObject()) {
                return JsonToNBT.getTagFromJson(GSON.toJson(json));
            } else {
                return JsonToNBT.getTagFromJson(JsonUtils.getString(json, "nbt"));
            }
        } catch (net.minecraft.nbt.NBTException e) {
            throw new JsonSyntaxException("Invalid NBT entry: " + e.toString());
        }
    }
}
