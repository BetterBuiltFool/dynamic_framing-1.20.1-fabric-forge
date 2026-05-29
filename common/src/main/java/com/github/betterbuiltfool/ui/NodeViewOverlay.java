package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.structure.Edge;
import com.github.betterbuiltfool.structure.JointNode;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

public class NodeViewOverlay {
    
    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((guiGraphics, deltaTRacker) -> {
            Minecraft client = Minecraft.getInstance();
            
            if (client.player == null || client.level == null) {
                return;
            }
        
        });
    }
    
    public static void renderOverlay(GuiGraphics guiGraphics, Minecraft client) {
    
    }
    
    public static void drawStructure(JointNode startingNode) {
    
    }
    
    public static void drawEdge(Edge edge) {
    
    }
    
    private static void drawLine(BlockPos start, BlockPos end) {
    
    }
    
    private static void drawNodeMarker(BlockPos pos) {
    
    }
}
