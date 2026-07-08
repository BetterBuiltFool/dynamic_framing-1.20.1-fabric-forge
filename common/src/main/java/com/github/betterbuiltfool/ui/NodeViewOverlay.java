package com.github.betterbuiltfool.ui;

import com.github.betterbuiltfool.items.RendersOverlay;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;

public class NodeViewOverlay {
    
    public static void renderOverlay(PoseStack poseStack) {
        Minecraft client = Minecraft.getInstance();
        
        if (client.player == null || client.level == null) {
            return;
        }
        var item = client.player.getMainHandItem();
        
        if (item.getItem() instanceof RendersOverlay toolItem) {
            toolItem.renderOverlay(client, poseStack, item);
        }
        
    }
}
