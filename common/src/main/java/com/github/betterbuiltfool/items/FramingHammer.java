package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;

import java.util.HashSet;
import java.util.List;

import com.github.betterbuiltfool.structure.JointNode;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.validation.ItemValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Framing tool for establishing the shape of a structure by placing nodes.
 */
public class FramingHammer extends Item implements RendersOverlay{
    
    public static final String ITEM_ID = "framing_hammer";
    
    public static final String FIRST_POS_DATA = "FirstPosData";
    
    public FramingHammer(Properties properties) {
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
                            "tooltip.dynamic_framing.framing_hammer.firstpos"
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
        var hammerTool = player.getMainHandItem();
        
        if (player.isShiftKeyDown()) {
            clearFirstPos(hammerTool);
            return InteractionResultHolder.consume(hammerTool);
        }
        
        var firstPos = getFirstPos(hammerTool);
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        if (firstPos == null) {
            return firstUse(hammerTool, lookPos);
        }
        
        return secondUse(level, player, firstPos, lookPos, hammerTool);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> firstUse(ItemStack hammerTool,
                                                                 BlockPos lookPos
    ) {
        setFirstPos(hammerTool, lookPos);
        return InteractionResultHolder.success(hammerTool);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> secondUse(
            Level level,
            Player player,
            BlockPos firstPos,
            BlockPos lookPos,
            ItemStack hammerTool
    ) {
        var secondPos = calcSecondPos(firstPos, lookPos);
        
        var offhandItem = player.getOffhandItem();
        
        var startNode = new JointNode(firstPos);
        var endNode = new JointNode(secondPos);
        
        if (!(offhandItem.getItem() instanceof BlockItem offhandBlock)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(hammerTool);
        }
        
        Block fillBlock = offhandBlock.getBlock();
        var edge = startNode.connectTo(endNode);
        
        if (!ItemValidator.validateStructureItem(offhandItem)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(hammerTool);
        }
        
        int materialCost = edge.getMaterialCost(level);
        
        Inventory inventory = player.getInventory();
        
        if (inventory.countItem(offhandItem.getItem()) < materialCost) {
            DynamicFraming.LOGGER.info("Not enough {} for edge length of {}", offhandItem.getDisplayName().getString(), materialCost);
            clearFirstPos(hammerTool);
            return InteractionResultHolder.consume(hammerTool);
        }
        
        edge.generateFill(level, fillBlock);
        removeMaterialCost(inventory, offhandItem, materialCost);
        
        clearFirstPos(hammerTool);
        return InteractionResultHolder.success(hammerTool);
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
     * @param hammerTool The ItemStack version of the tool
     * @return BlockPos of first set point or null if none.
     */
    public static @Nullable BlockPos getFirstPos(@NotNull ItemStack hammerTool) {
        // TODO: Extract this to common class for use by other tools
        CompoundTag firstPosTag = hammerTool.getTagElement(FIRST_POS_DATA);
        
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
     * @param hammerTool The ItemStack version of the tool
     * @param lookPos The BlockPos to be stored as the first position
     */
    private void setFirstPos(
            @NotNull ItemStack hammerTool,
            @NotNull BlockPos lookPos
    ) {
        // TODO: Extract this to common class for use by other tools
        CompoundTag firstPosTag = hammerTool.getOrCreateTag();
        
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", lookPos.getX());
        posTag.putInt("Y", lookPos.getY());
        posTag.putInt("Z", lookPos.getZ());
        
        firstPosTag.put(FIRST_POS_DATA, posTag);
    }
    
    /**
     * Clears the FirstPos tag from the tool item.
     *
     * @param hammerTool The ItemStack version of the tool
     */
    private void clearFirstPos( @NotNull ItemStack hammerTool) {
        // TODO: Extract this to common class for use by other tools
        hammerTool.removeTagKey(FIRST_POS_DATA);
    }
    
    @Override
    public void renderOverlay(Minecraft client,
                              PoseStack poseStack,
                              @NotNull ItemStack itemStack
    ) {
        LongOpenHashSet nodes = new LongOpenHashSet();
        BlockPos firstPos = getFirstPos(itemStack);
        if (firstPos != null) {
            nodes.add(firstPos.asLong());
        }
        FramingHammerOverlay.renderOverlay(client, poseStack, nodes);
    }
    
    public @NotNull Iterable<JointNode> getNodes(@NotNull ItemStack itemStack) {
        HashSet<JointNode> nodes = new HashSet<>();
        
        BlockPos firstPos = FramingHammer.getFirstPos(itemStack);
        if (firstPos != null) {
            nodes.add(new JointNode(firstPos));
        }
        
        return nodes;
    }
}