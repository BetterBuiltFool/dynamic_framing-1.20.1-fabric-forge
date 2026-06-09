package com.github.betterbuiltfool.fabric.client;

import com.github.betterbuiltfool.ui.NodeViewOverlay;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class GuiOverlayFabric {
    
    public static void registerGui() {
        WorldRenderEvents.LAST.register(context -> NodeViewOverlay.renderOverlay(context.matrixStack()));
    }
}
