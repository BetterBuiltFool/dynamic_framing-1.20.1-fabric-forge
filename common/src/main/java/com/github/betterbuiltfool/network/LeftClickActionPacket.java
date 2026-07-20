package com.github.betterbuiltfool.network;

import com.github.betterbuiltfool.items.HasLeftClickUse;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;

import java.util.function.Supplier;

public class LeftClickActionPacket {
    
    public void encode(FriendlyByteBuf buffer) {
    }
    
    public static LeftClickActionPacket decode(FriendlyByteBuf buffer) {
        return new LeftClickActionPacket();
    }
    
    public void handle(Supplier<NetworkManager.PacketContext> contextSupplier) {
        var context = contextSupplier.get();
        context.queue(() -> {
            if (context.getPlayer() instanceof ServerPlayer player) {
                var itemstack = player.getMainHandItem();
                
                if (itemstack.getItem() instanceof HasLeftClickUse leftClickItem) {
                    leftClickItem.useLeftClick(player.serverLevel(), player, InteractionHand.MAIN_HAND);
                }
            }
        });
    }
}
