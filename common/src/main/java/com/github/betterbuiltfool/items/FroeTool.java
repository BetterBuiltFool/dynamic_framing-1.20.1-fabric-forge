package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class FroeTool extends Item {
    
    public static final String ITEM_ID = "froe";
    
    private static final Set<TagKey<Item>> whitelist = new HashSet<>();
    
    static{
        // TODO: Read this in from config file
        whitelist.add(ItemTags.LOGS);
    }
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    public boolean validateOffhand(
            ItemStack offhandItem
    ) {
        
        for (TagKey<Item> tag : whitelist) {
            if (offhandItem.is(tag)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        if (level.isClientSide()) {
            return super.use(level, player, usedHand);
        }
        
        {  // TODO: move to method
            DynamicFraming.LOGGER.info("Used froe");
            ItemStack offhandItem = player.getOffhandItem();
            if (!validateOffhand(offhandItem)) {
                DynamicFraming.LOGGER.info("Invalid offhand item: {}; cannot place!", offhandItem);
                return super.use(level, player, usedHand);
            }
            DynamicFraming.LOGGER.info("Valid offhand item: {}", offhandItem);
        }
        
        return super.use(level, player, usedHand);
    }
}