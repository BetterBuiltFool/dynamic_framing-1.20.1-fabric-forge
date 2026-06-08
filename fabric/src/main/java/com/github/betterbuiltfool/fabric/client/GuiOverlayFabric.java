package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.ui.NodeViewOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class GuiOverlayFabric implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            NodeViewOverlay.renderOverlay(context.matrixStack(), context.worldRenderer());
        });
    }
}
