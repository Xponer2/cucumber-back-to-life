package com.blakebr0.cucumber.client;

import io.github.xponer2.translator.client.RenderSystem;
import io.github.xponer2.translator.client.RenderType;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL14;

public final class ModRenderTypes {
    // blocks drawn at 25% opacity through other blocks (structure previews)
    public static final RenderType GHOST = new RenderType("cucumber:ghost",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.25F);
                RenderSystem.disableDepthTest();
            },
            () -> {
                RenderSystem.enableDepthTest();
                GL14.glBlendColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });
}
