package com.blakebr0.cucumber.compat.almostunified;

import net.minecraft.item.Item;

/**
 * Almost Unified (preferred items of tags) has no 1.12.2 version: there is never a preferred item.
 */
public class AlmostUnifiedAdapter {
    public static boolean isLoaded() {
        return false;
    }

    public static Item getPreferredItemForTag(String tagId) {
        return null;
    }
}
