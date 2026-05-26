package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.betterbuiltfool.structure.Node;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
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
                            "tooltip.dynamic_framing.froe.firstpos"
                    ).append(firstPos.toShortString())
            );
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        var froeTool = player.getMainHandItem();
        
        if (player.isShiftKeyDown()) {
            clearFirstPos(froeTool);
            return InteractionResultHolder.consume(froeTool);
        }
        
        var firstPos = getFirstPos(froeTool);
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        if (firstPos == null) {
            return firstUse(froeTool, lookPos);
        }
        
        return secondUse(level, player, firstPos, lookPos, froeTool);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> firstUse(ItemStack froeTool,
                                                                 BlockPos lookPos
    ) {
        setFirstPos(froeTool, lookPos);
        return InteractionResultHolder.success(froeTool);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> secondUse(
            Level level,
            Player player,
            BlockPos firstPos,
            BlockPos lookPos,
            ItemStack froeTool
    ) {
        var secondPos = calcSecondPos(firstPos, lookPos);
        
        var offhandItem = player.getOffhandItem();
        
        var startNode = new Node(firstPos);
        var endNode = new Node(secondPos);
        
        if (!(offhandItem.getItem() instanceof BlockItem offhandBlock)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(froeTool);
        }
        
        var edge = startNode.edgeToNode(endNode, offhandBlock.getBlock());
        
        if (!validateOffhand(offhandItem)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(froeTool);
        }
        
        int materialCost = edge.getMaterialCost();
        
        Inventory inventory = player.getInventory();
        
        if (inventory.countItem(offhandItem.getItem()) < materialCost) {
            DynamicFraming.LOGGER.info("Not enough {} for edge length of {}", offhandItem.getDisplayName().getString(), materialCost);
            clearFirstPos(froeTool);
            return InteractionResultHolder.consume(froeTool);
        }
        
        edge.generateFill(level);
        removeMaterialCost(inventory, offhandItem, materialCost);
        
        clearFirstPos(froeTool);
        return InteractionResultHolder.success(froeTool);
    }
    
    /**
     * Extracts the material cost from the given inventory, preferentially removing first from the inventory, and
     * removing the remainder from the offhand stack.
     * <p>
     * Note this will not fail if the inventory does not have enough items.
     *
     * @param inventory The source inventory that supplies raw materials.
     * @param offhandItem The item type to be removed, and secondary source of raw materials
     * @param materialCost The total amount of materials to be extracted.
     */
    private void removeMaterialCost(
            @NotNull Inventory inventory,
            @NotNull ItemStack offhandItem,
            int materialCost
    ) {
        int amountRemoved = 0;
        for (ItemStack slotItem: inventory.items){
            if (slotItem.getItem() != offhandItem.getItem()) {
                continue;
            }
            int slotCount = slotItem.getCount();
            amountRemoved += slotCount;
            
            if (amountRemoved >= materialCost) {
                int amountUsed = amountRemoved - materialCost;
                slotItem.setCount(amountUsed);
                break;
            } else {
                slotItem.setCount(0);
            }
        }
        if (amountRemoved < materialCost) {
            int amountUsed = materialCost - amountRemoved;
            offhandItem.setCount(offhandItem.getCount()-amountUsed);
        }
    
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
        
        var delta = lookPos.subtract(firstPos);
        var direction = Direction.getNearest(delta.getX(), delta.getY(), delta.getZ());
        var axis = direction.getAxis();
        
        return firstPos.relative(axis, delta.get(axis));
    }
    
    /**
     * Extracts the first set pos from the itemstack tags.
     * If null, first pos is unset.
     *
     * @param froeTool The ItemStack version of the tool
     * @return BlockPos of first set point or null if none.
     */
    private static @Nullable BlockPos getFirstPos(@NotNull ItemStack froeTool) {
        // TODO: Extract this to common class for use by other tools
        CompoundTag firstPosTag = froeTool.getTagElement(FIRST_POS_DATA);
        
        if (firstPosTag == null) {
            return null;
        }
        
        return new BlockPos(
                firstPosTag.getInt("X"),
                firstPosTag.getInt("Y"),
                firstPosTag.getInt("Z")
        );
        
    }
    
    /**
     * Creates or modifies a tag on the tool with the first set position.
     *
     * @param froeTool The ItemStack version of the tool
     * @param lookPos The BlockPos to be stored as the first position
     */
    private void setFirstPos(
            @NotNull ItemStack froeTool,
            @NotNull BlockPos lookPos
    ) {
        // TODO: Extract this to common class for use by other tools
        CompoundTag firstPosTag = froeTool.getOrCreateTag();
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", lookPos.getX());
        posTag.putInt("Y", lookPos.getY());
        posTag.putInt("Z", lookPos.getZ());
        
        firstPosTag.put(FIRST_POS_DATA, posTag);
    }
    
    /**
     * Clears the FirstPos tag from the tool item.
     *
     * @param froeTool The ItemStack version of the tool
     */
    private void clearFirstPos( @NotNull ItemStack froeTool) {
        // TODO: Extract this to common class for use by other tools
        froeTool.removeTagKey(FIRST_POS_DATA);
    }
    
    public boolean validateOffhand(
            ItemStack offhandItem
    ) {
        // TODO: Extract this to common class for use by other tools
        // Class name StructureUtils?
        
        for (TagKey<Item> tag : whitelist) {
            if (offhandItem.is(tag)) {
                return true;
            }
        }
        
        return false;
    }
}