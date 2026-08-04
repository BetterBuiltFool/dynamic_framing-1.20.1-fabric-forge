package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.config.CommonConfig;
import com.github.betterbuiltfool.items.nbtHelper.FroeData;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FroeTool extends Item implements RendersOverlay, SuppressesEquipAnimation{
    public static final String ITEM_ID = "froe";
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    public boolean shouldCauseReequipAnimation(
            ItemStack oldStack,
            ItemStack newStack,
            boolean slotChanged
    ) {
        if (slotChanged) {
            return true;
        }
        
        if (this.shouldSuppressReequip(oldStack, newStack)) {
            return false;
        }
        
        return oldStack.getItem() != newStack.getItem();
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
            contextBuilder.addFirstPos(selection.posA()).addSecondPos(selection.posB());
        }
        FramingHammerOverlay.renderOverlay(contextBuilder.build());
    }
}
