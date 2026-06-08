package com.github.betterbuiltfool.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class GuiOverlayFabric implements ClientModInitializer {
    
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(context -> {
            // TODO: call overlay method
        });
    }
}
