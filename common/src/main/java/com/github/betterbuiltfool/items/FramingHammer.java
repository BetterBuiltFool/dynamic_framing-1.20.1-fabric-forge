package com.github.betterbuiltfool.items;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.client.ClientLocalNodes;
import com.github.betterbuiltfool.data.FramedStructureStorage;
import com.github.betterbuiltfool.items.nbtHelper.FramingHammerData;
import com.github.betterbuiltfool.structure.JointNode;
import com.github.betterbuiltfool.ui.overlays.FramingHammerOverlay;
import com.github.betterbuiltfool.ui.overlays.NodeOverlayContextBuilder;
import com.github.betterbuiltfool.validation.EdgeValidator;
import com.github.betterbuiltfool.validation.ItemValidator;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
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

import java.util.List;

/**
 * A Framing tool for establishing the shape of a structure by placing nodes.
 */
public class FramingHammer extends Item implements RendersOverlay{
    
    public static final String ITEM_ID = "framing_hammer";
    
    public FramingHammer(Properties properties) {
        super(properties);
    }
    
    @Override
    public void inventoryTick(ItemStack stack,
                              Level level,
                              Entity entity,
                              int slotId,
                              boolean isSelected
    ) {
        if (!isSelected || !(entity instanceof Player player) || level.isClientSide()) {
            return;
        }
        
        var hammerTool = new FramingHammerData(stack);
        
        if (!hammerTool.hasFirstPos()) {
            return;
        }
        
        long firstPos = hammerTool.getFirstPos();
        
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        hammerTool.setSecondPos(calcSecondPos(BlockPos.of(firstPos), lookPos).asLong());
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(
            Level level,
            Player player,
            InteractionHand usedHand
    ) {
        var hammerTool = new FramingHammerData(player.getMainHandItem());
        
        if (player.isShiftKeyDown()) {
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        var ray = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        var lookPos = ray.getBlockPos();
        
        if (!hammerTool.hasFirstPos()) {
            return firstUse(hammerTool, lookPos);
        }
        
        return secondUse(level, player, hammerTool);
    }
    
    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltipComponents,
            TooltipFlag isAdvanced
    ) {
        var tool = new FramingHammerData(stack);
        
        if (tool.hasFirstPos()) {
            tooltipComponents.add(
                    Component.translatable(
                            "tooltip.dynamic_framing.framing_hammer.firstpos"
                             )
                             .append(BlockPos.of(tool.getFirstPos())
                                             .toShortString())
            );
        }
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> firstUse(
            FramingHammerData hammerTool,
            BlockPos lookPos
    ) {
        hammerTool.setFirstPos(lookPos.asLong());
        return InteractionResultHolder.success(hammerTool.wrapped);
    }
    
    private @NotNull InteractionResultHolder<ItemStack> secondUse(
            Level level,
            Player player,
            FramingHammerData hammerTool
    ) {
        if (!hammerTool.hasSecondPos()) {
            DynamicFraming.LOGGER.info("No second node detected. This shouldn't be happening.");
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        var firstPos = hammerTool.getFirstPos();
        var secondPos = hammerTool.getSecondPos();
        
        if (!EdgeValidator.validate(level, firstPos, secondPos)) {
            DynamicFraming.LOGGER.info("New edge obstructed!");
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        var offhandItem = player.getOffhandItem();
        
        // TODO: remove edge generation and add to Froe tool
        var startNode = new JointNode(BlockPos.of(firstPos));
        var endNode = new JointNode(BlockPos.of(secondPos));
        
        if (!(offhandItem.getItem() instanceof BlockItem offhandBlock)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        Block fillBlock = offhandBlock.getBlock();
        var edge = startNode.connectTo(endNode);
        
        if (!ItemValidator.validateStructureItem(offhandItem)) {
            DynamicFraming.LOGGER.info("Invalid offhand item {}", offhandItem.getDisplayName());
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        if (!level.isClientSide) {
            var storage = FramedStructureStorage.get(level);
            var structureGraph = FramedStructureStorage.getOrCreateDimensionGraph(level);
            
            structureGraph.connect(firstPos, secondPos);
            storage.setDirty();
        }
        
        int materialCost = edge.getMaterialCost(level);
        
        Inventory inventory = player.getInventory();
        
        if (inventory.countItem(offhandItem.getItem()) < materialCost) {
            DynamicFraming.LOGGER.info("Not enough {} for edge length of {}", offhandItem.getDisplayName().getString(), materialCost);
            hammerTool.clear();
            return InteractionResultHolder.consume(hammerTool.wrapped);
        }
        
        if (!level.isClientSide) {
            edge.generateFill(level, fillBlock);
            removeMaterialCost(inventory, offhandItem, materialCost);
        }
        hammerTool.clear();
        return InteractionResultHolder.success(hammerTool.wrapped);
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
    
    @Override
    public void renderOverlay(Minecraft client,
                              PoseStack poseStack,
                              @NotNull ItemStack itemStack
    ) {
        ClientLocalNodes.requestRefresh(client);
        var hammerTool = new FramingHammerData(itemStack);
        
        var highlightNodes = new LongOpenHashSet();
        var contextBuilder =
                new NodeOverlayContextBuilder(client, poseStack).addNodeMap(ClientLocalNodes.getLocalNodes())
                                                                .addHighlightPos(highlightNodes);
        // TODO: This is a mess, clean it up.
        contextBuilder = hammerTool.hasFirstPos() ? contextBuilder.addHighlightPos(hammerTool.getFirstPos())
                                                  : contextBuilder;  // TODO: remove debug line
        contextBuilder = hammerTool.hasFirstPos() ? contextBuilder.addFirstPos(hammerTool.getFirstPos())
                                                  : contextBuilder;
        contextBuilder = hammerTool.hasSecondPos() ? contextBuilder.addHighlightPos(hammerTool.getSecondPos())
                                                   : contextBuilder;  // TODO: remove debug line
        contextBuilder = hammerTool.hasSecondPos() ? contextBuilder.addSecondPos(hammerTool.getSecondPos())
                                                   : contextBuilder;
        FramingHammerOverlay.renderOverlay(contextBuilder.build());
    }
}