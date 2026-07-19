package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.items.nbtHelper.FramingHammerData;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class ClientAnimationSuppressor {
    
    public static boolean onlyChangedNBT(ItemStack oldStack,
                                         ItemStack newStack
    ) {
        if (oldStack.isEmpty() || newStack.isEmpty()) {
            return false;
        }
        if (oldStack.getItem() != newStack.getItem()) {
            return false;
        }
        
        var oldTag = oldStack.hasTag() ? oldStack.getTag()
                                                 .copy() : null;
        var newTag = newStack.hasTag() ? oldStack.getTag()
                                                 .copy() : null;
        
        if (oldTag != null) {
            oldTag.remove(FramingHammerData.CONTAINER_KEY);
        }
        if (newTag != null) {
            newTag.remove(FramingHammerData.CONTAINER_KEY);
        }
        
        return Objects.equals(oldTag, newTag);
    }
}
