package com.github.betterbuiltfool.fabric.mixin.client;

import com.github.betterbuiltfool.items.SuppressesEquipAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
public class MixinFabricHandRenderer {
    
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    
    @Redirect(
            method = "tick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;" +
                             "Lnet/minecraft/world/item/ItemStack;)Z"
            )
    
    )
    private boolean redirectItemMatchCheck(ItemStack oldStack,
                                           ItemStack newStack
    ) {
        if (oldStack.getItem() instanceof SuppressesEquipAnimation suppressedTool) {
            if (suppressedTool.shouldSuppressReequip(oldStack, newStack)) {
                return true;
            }
        }
        return ItemStack.matches(oldStack, newStack);
    }
}