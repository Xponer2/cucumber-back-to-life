package com.blakebr0.cucumber.client.extensions;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;

/**
 * Tilts the watering can while it is used (registered as client item extension, see the Translator).
 */
public class WateringCanClientItemExtensions implements io.github.xponer2.translator.client.IClientItemExtensions {
    public static final WateringCanClientItemExtensions INSTANCE = new WateringCanClientItemExtensions();

    @Override
    public boolean applyHandTransform(EntityPlayerSP player, EnumHandSide arm, ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
        if (player.isHandActive() && player.getItemInUseCount() > 0) {
            float f = (float) (player.getItemInUseCount() % 20);
            float f1 = f - partialTick + 1.0F;
            float f2 = 1.0F - f1 / 20.0F;
            float f7 = -10.0F + 10.0F * MathHelper.cos(f2 * 2.0F * (float) Math.PI);

            GlStateManager.rotate(f7, 1.0F, 0.0F, 0.0F);
        }

        return false;
    }
}
