package com.github.betterbuiltfool.mixin.client;

import com.github.betterbuiltfool.client.ClientLeftClickInterception;
import com.github.betterbuiltfool.items.FramingHammer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MixinMinecraftLeftClick {
    
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void handleLeftClick(CallbackInfoReturnable<Boolean> callbackInfo) {
        var client = Minecraft.getInstance();
        
        if (ClientLeftClickInterception.tryConsumeLeftClick(client)) {
            callbackInfo.setReturnValue(true);
        }
    }
    
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void blockContinuousMining(boolean leftClickPressed,
                                       CallbackInfo callbackInfo
    ) {
        var client = Minecraft.getInstance();
        
        var player = client.player;
        
        if (player == null) {
            return;
        }
        
        ItemStack itemStack = player.getMainHandItem();
        
        if (itemStack.getItem() instanceof FramingHammer hammerTool) {
            if (hammerTool.shouldBlockMining(player, itemStack)) {
                callbackInfo.cancel();
            }
        }
    }
}
