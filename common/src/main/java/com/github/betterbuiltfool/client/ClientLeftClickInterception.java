package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.items.HasLeftClickUse;
import com.github.betterbuiltfool.network.DynamicFramingNetworking;
import com.github.betterbuiltfool.network.LeftClickActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

public class ClientLeftClickInterception {
    
    public static boolean tryConsumeLeftClick(Minecraft minecraft) {
        var player = minecraft.player;
        
        if (player == null || minecraft.screen != null) {
            return false;
        }
        
        var itemStack = player.getMainHandItem();
        
        if (!(itemStack.getItem() instanceof HasLeftClickUse leftClickItem)) {
            return false;
        }
        
        var result = leftClickItem.useLeftClick(player.level(), player, InteractionHand.MAIN_HAND);
        
        if (!result.consumesAction()) {
            return false;
        }
        
        DynamicFramingNetworking.CHANNEL.sendToServer(new LeftClickActionPacket());
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }
}
