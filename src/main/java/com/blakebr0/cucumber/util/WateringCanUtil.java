package com.blakebr0.cucumber.util;

import com.blakebr0.cucumber.client.sound.WateringCanSound;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public final class WateringCanUtil {
    public static void startPlayingSound(EntityPlayer player) {
        if (player.world.isRemote && !WateringCanSound.playing(player.getEntityId())) {
            Minecraft.getMinecraft().getSoundHandler().playSound(new WateringCanSound(player));
        }
    }

    /**
     * Stops playing the watering can sound for the given player on the client side
     * @param player the player
     */
    public static void stopPlayingSound(EntityPlayer player) {
        if (player.world.isRemote && WateringCanSound.playing(player.getEntityId())) {
            WateringCanSound.stop(player.getEntityId());
        }
    }
}
