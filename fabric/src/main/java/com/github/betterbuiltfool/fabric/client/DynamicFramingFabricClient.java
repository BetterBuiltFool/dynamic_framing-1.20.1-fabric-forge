package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.client.DynamicFramingClient;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DynamicFramingFabricClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(DynamicFraming.MOD_ID + "_fabric_client");
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        GuiOverlayFabric.registerGui();
        DynamicFramingClient.init();
        FabricModelLoader.register();
    }
}
