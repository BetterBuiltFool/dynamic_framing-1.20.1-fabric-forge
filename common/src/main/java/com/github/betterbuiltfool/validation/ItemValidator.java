package com.github.betterbuiltfool.validation;

import com.github.betterbuiltfool.config.CommonConfig;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ItemValidator {
    
    public static @Nullable BlockItem validatedStructureItem(ItemStack item) {
        if (!(item.getItem() instanceof BlockItem blockItem) || !(item.is(ItemTags.LOGS))) {
            return null;
        }
        for (TagKey<Item> tag : CommonConfig.structureMaterialBlacklist.tags()) {
            if (item.is(tag)) {
                return null;
            }
        }
        return blockItem;
    }
}
