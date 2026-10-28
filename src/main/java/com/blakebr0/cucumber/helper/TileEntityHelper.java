package com.blakebr0.cucumber.helper;

import io.github.xponer2.translator.util.Platform;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class TileEntityHelper {
    public static void dispatchToNearbyPlayers(TileEntity tile) {
        var level = tile.getWorld();
        if (level == null)
            return;

        var packet = tile.getUpdatePacket();
        if (packet == null)
            return;

        var players = level.playerEntities;
        var pos = tile.getPos();

        for (var player : players) {
            if (player instanceof EntityPlayerMP mPlayer) {
                if (isPlayerNearby(mPlayer.posX, mPlayer.posZ, pos.getX() + 0.5, pos.getZ() + 0.5) && Platform.isChunkSentTo(mPlayer, pos)) {
                    mPlayer.connection.sendPacket(packet);
                }
            }
        }
    }

    public static void dispatchToNearbyPlayers(World level, int x, int y, int z) {
        var tile = level.getTileEntity(new BlockPos(x, y, z));
        if (tile != null) {
            dispatchToNearbyPlayers(tile);
        }
    }

    private static boolean isPlayerNearby(double x1, double z1, double x2, double z2) {
        return Math.hypot(x1 - x2, z1 - z2) < 64;
    }
}
