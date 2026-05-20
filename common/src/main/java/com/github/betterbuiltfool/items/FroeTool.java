package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FroeTool extends Item {
    
    public static final String ITEM_ID = "froe";
    
    private static final Set<TagKey<Item>> whitelist = new HashSet<>();
    public static final String FIRST_POS_DATA = "FirstPosData";
    
    static{
        // TODO: Read this in from config file
        whitelist.add(ItemTags.LOGS);
    }
    
    public FroeTool(Properties properties) {
        super(properties);
    }
    
    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced
    ) {
        var firstPos = getFirstPos(stack);
        
        if (firstPos != null) {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.dynamic_framing.froe.firstpos").append(firstPos.toShortString()
                    )
            );
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
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
        var froeTool = player.getMainHandItem();
        
        var firstPos = getFirstPos(froeTool);
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        if (firstPos == null) {
            setFirstPos(froeTool, lookPos);
            return InteractionResultHolder.success(froeTool);
        }
        
        var secondPos = calcSecondPos(firstPos, lookPos);
        
        var offhandItem = player.getOffhandItem();
        
        if (!validateOffhand(offhandItem)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.fail(froeTool);
        }
        
        int materialCost = calcMaterialCost(firstPos, secondPos);
        
        if (inventoryCount(player.getInventory(), offhandItem) < materialCost) {
            DynamicFraming.LOGGER.info("Not enough {} for edge length of {}", offhandItem.getDisplayName(), materialCost);
            return InteractionResultHolder.fail(froeTool);
        }
        
        tryPlaceEdge(firstPos, secondPos);
        removeMaterialCost(player.getInventory(), offhandItem);
        
        return InteractionResultHolder.success(froeTool);
    }
    
    private void removeMaterialCost(
            @NotNull Inventory inventory,
            @NotNull ItemStack offhandItem
    ) {
    
    }
    
    private void tryPlaceEdge(
            @NotNull BlockPos firstPos,
            @NotNull BlockPos secondPos
    ) {
    
    }
    
    private int inventoryCount(
            @NotNull Inventory inventory,
            @NotNull ItemStack offhandItem
    ) {
        return 0;
    }
    
    private int calcMaterialCost(
            @NotNull BlockPos firstPos,
            @NotNull BlockPos secondPos
    ) {
        return 0;
    }
    
    /**
     * Calculates the second position of an edge based on where the player is looking.
     *
     * @param firstPos The starting position of the edge
     * @param lookPos The position at which the player is looking
     * @return A BlockPos that is coaxial to the firstPos
     */
    private @NotNull BlockPos calcSecondPos(
            @NotNull BlockPos firstPos,
            @NotNull BlockPos lookPos
    ) {
        BlockPos secondPos;
        
        var delta = lookPos.subtract(firstPos);
        
        // Find the longest axis in the difference between position
        var max = Math.max(
                Math.abs(delta.getX()),
                Math.max(
                        Math.abs(delta.getY()),
                        Math.abs(delta.getZ())
                )
        );
        
        if (max == delta.getX()) {
            // Align along X axis
            secondPos = new BlockPos(lookPos.getX(), firstPos.getY(), firstPos.getZ());
        } else if (max == delta.getY()) {
            // Align along Y axis
            secondPos = new BlockPos(firstPos.getX(), lookPos.getY(), firstPos.getZ());
        } else {
            // Align along Z axis
            secondPos = new BlockPos(firstPos.getX(), firstPos.getY(), lookPos.getZ());
        }
        
        return secondPos;
    }
    
    private static @Nullable BlockPos getFirstPos(@NotNull ItemStack item) {
        if (!(item.hasTag() && item.getTag().contains(FIRST_POS_DATA))) {
            return null;
        }
        CompoundTag firstPosTag = item.getTag().getCompound(FIRST_POS_DATA);
        
        return new BlockPos(
                firstPosTag.getInt("X"),
                firstPosTag.getInt("Y"),
                firstPosTag.getInt("Z")
        );
        
    }
    
    private void setFirstPos(
            @NotNull ItemStack froeTool,
            @NotNull BlockPos lookPos
    ) {
        CompoundTag firstPosTag = froeTool.getOrCreateTag();
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", lookPos.getX());
        posTag.putInt("Y", lookPos.getY());
        posTag.putInt("Z", lookPos.getZ());
        
        firstPosTag.put(FIRST_POS_DATA, posTag);
    }
}