package com.github.betterbuiltfool.items;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface RendersOverlay {
    
    public void renderOverlay(Minecraft client, PoseStack poseStack, @NotNull ItemStack itemStack);
    
}
