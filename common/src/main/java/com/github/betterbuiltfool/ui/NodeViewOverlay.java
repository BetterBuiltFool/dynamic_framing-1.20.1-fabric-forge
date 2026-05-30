package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.items.FroeTool;
import com.github.betterbuiltfool.structure.Edge;
import com.github.betterbuiltfool.structure.JointNode;
import dev.architectury.event.events.client.ClientGuiEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class NodeViewOverlay {
    
    private static final ResourceLocation NODE_MARKER = new ResourceLocation(
            DynamicFraming.MOD_ID, "textures/ui/node_marker"
    );
    
    public static void register() {
        ClientGuiEvent.RENDER_HUD.register((guiGraphics, deltaTRacker) -> {
            Minecraft client = Minecraft.getInstance();
            
            if (client.player == null || client.level == null) {
                return;
            }
            
            var item = client.player.getMainHandItem();
            
            if (item.getItem() instanceof FroeTool) {
                DynamicFraming.LOGGER.info("Tool Equipped");
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
    
    private static ScreenPos worldToScreen(BlockPos worldPos, Matrix4f projection) {
//        Minecraft client = Minecraft.getInstance();
        
//        GameRenderer renderer = client.gameRenderer;
//        Matrix4f projection = renderer.getProjectionMatrix(client.getWindow().getGuiScale());
        
        var center = worldPos.getCenter();
        
        var vec4WorldPos = new Vector4f(
                (float) center.x,
                (float) center.y,
                (float) center.z,
                1.0f
        );
        
        var screenPos = projection.transform(vec4WorldPos);
        
        return new ScreenPos((int) screenPos.x, (int) screenPos.y);
        
    }
    
    private static void renderNodeMarker(BlockPos pos) {
    
    }
}
