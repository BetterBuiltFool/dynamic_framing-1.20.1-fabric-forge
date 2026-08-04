package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.config.CommonConfig;
import com.github.betterbuiltfool.items.nbtHelper.FroeData;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class FroeTool extends Item implements RendersOverlay, SuppressesEquipAnimation{
    public static final String ITEM_ID = "froe";
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    @Override
    public void renderOverlay(Minecraft client,
                              PoseStack poseStack,
                              @NotNull ItemStack itemStack
    ) {
        ClientLocalNodes.requestRefresh(client);
        var froeTool = new FroeData(itemStack);
        
        var contextBuilder = new NodeOverlayContextBuilder(client, poseStack).addNodeMap(ClientLocalNodes.getLocalNodes());
        
        if (froeTool.hasSelection()) {
            var selection = froeTool.getSelection();
            if (EdgeValidator.validate(client.level, selection.posA(), selection.posB())) {
                contextBuilder.setHighlightColor(CommonConfig.validEdgeColor);
            } else {
                contextBuilder.setHighlightColor(CommonConfig.invalidEdgeColor);
            }
        }
        FramingHammerOverlay.renderOverlay(contextBuilder.build());
    }
}
