package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.client.ClientAnimationSuppressor;
import net.minecraft.world.item.ItemStack;

public interface SuppressesEquipAnimation {
    default boolean shouldSuppressReequip(ItemStack oldStack,
                                          ItemStack newStack
    ) {
        return ClientAnimationSuppressor.onlyChangedNBT(oldStack, newStack);
    }
}
