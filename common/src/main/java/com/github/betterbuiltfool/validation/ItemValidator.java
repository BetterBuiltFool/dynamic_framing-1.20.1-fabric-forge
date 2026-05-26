package com.github.betterbuiltfool.validation;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ItemValidator {
    
    private static final Set<TagKey<Item>> structureItemWhitelist = new HashSet<>();
    
    static{
        // TODO: Read this in from config file
        structureItemWhitelist.add(ItemTags.LOGS);
    }
    
    public static boolean validateStructureItem(ItemStack item) {
        for (TagKey<Item> tag : structureItemWhitelist) {
            if (item.is(tag)) {
                return true;
            }
        }
        
        return false;
    }
}
