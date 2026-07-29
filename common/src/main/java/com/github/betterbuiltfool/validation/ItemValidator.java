package com.github.betterbuiltfool.validation;

import com.github.betterbuiltfool.config.CommonConfig;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemValidator {
    
    public static boolean validateStructureItem(ItemStack item) {
        for (TagKey<Item> tag : CommonConfig.structureMaterialWhitelist.tags()) {
            if (item.is(tag)) {
                return true;
            }
        }
        
        return false;
    }
}
