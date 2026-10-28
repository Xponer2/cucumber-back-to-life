package com.blakebr0.cucumber.event;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The 1.21 mixin that fires {@link ItemBreakEvent}, on the 1.12.2 Forge event: when a player's item breaks.
 */
public final class ItemBreakEventHandler {
    @SubscribeEvent
    public void onPlayerDestroyItem(PlayerDestroyItemEvent event) {
        var player = event.getEntityPlayer();
        if (player.world instanceof WorldServer world) {
            MinecraftForge.EVENT_BUS.post(new ItemBreakEvent(event.getOriginal().copy(), 1, world, player));
        }
    }
}
