package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.client.DynamicFramingClient;
import net.fabricmc.api.ClientModInitializer;

public final class DynamicFramingFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        GuiOverlayFabric.registerGui();
        FabricModelLoaderHook.register();
        DynamicFramingClient.init();
    }
}
