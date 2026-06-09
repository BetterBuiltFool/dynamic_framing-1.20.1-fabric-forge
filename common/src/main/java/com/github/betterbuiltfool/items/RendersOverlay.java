package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.structure.JointNode;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface RendersOverlay {
    
    public @NotNull Iterable<JointNode> getNodes(@NotNull ItemStack itemStack);
    
}
