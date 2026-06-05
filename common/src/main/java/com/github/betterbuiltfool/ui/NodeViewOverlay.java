package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.FroeTool;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.*;

public class NodeViewOverlay {
    
    private static final ResourceLocation NODE_MARKER = new ResourceLocation(
            DynamicFraming.MOD_ID, "textures/ui/node_marker.png"
    );
    
    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((guiGraphics, deltaTracker) -> {
            Minecraft client = Minecraft.getInstance();
            
            if (client.player == null || client.level == null) {
                return;
            }
            var item = client.player.getMainHandItem();
            
            if (item.getItem() instanceof FroeTool) {
                // TODO: add a base class or interface for approving tools
                DynamicFraming.LOGGER.info("Tool Equipped");
                
                
                var currentNodePos = FroeTool.getFirstPos(item);
                if (currentNodePos != null) {
                }
            }
        
        });
    }
}
