package com.github.betterbuiltfool.items;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface HasLeftClickUse {
    
    default InteractionResult useLeftClick(Level level,
                                           Player player,
                                           InteractionHand interactionHand
    ) {
        return InteractionResult.PASS;
    }
    
    default boolean shouldBlockMining(Player player,
                                      ItemStack itemStack
    ) {
        return false;
    }
}
