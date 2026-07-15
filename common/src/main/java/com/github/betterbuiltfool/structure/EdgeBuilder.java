package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.validation.BlockPosValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;


public class EdgeBuilder {
    
    public static void build(
            Level level,
            long firstPos,
            long secondPos,
            Block edgeMaterial
    ) {
        var startPos = BlockPos.of(firstPos);
        var endPos = BlockPos.of(secondPos);
        
        var directionVector = startPos.subtract(endPos);
        var facing = Direction.getNearest(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
        
        var blockState = edgeMaterial.defaultBlockState()
                                     .setValue(BlockStateProperties.AXIS, facing.getAxis());
        
        BlockPos.betweenClosedStream(BlockPos.of(firstPos), BlockPos.of(secondPos))
                .filter(blockPos -> BlockPosValidator.validate(level, blockPos))
                .forEach(pos -> level.setBlockAndUpdate(pos, blockState));
        
    }
    
    public static int getMaterialCost(
            Level level,
            long firstPos,
            long secondPos
    ) {
        return Math.toIntExact(
                BlockPos.betweenClosedStream(BlockPos.of(firstPos), BlockPos.of(secondPos))
                        .filter(blockPos -> BlockPosValidator.validate(level, blockPos))
                        .count()
        );
    }
    
    /**
     * Extracts the material cost from the given inventory, preferentially removing first from the inventory, and
     * removing the remainder from the offhand stack.
     * <p>
     * Note this will not fail if the inventory does not have enough items.
     *
     * @param inventory    The source inventory that supplies raw materials.
     * @param offhandItem  The item type to be removed, and secondary source of raw materials
     * @param materialCost The total amount of materials to be extracted.
     */
    private void removeMaterialCost(
            @NotNull Inventory inventory,
            @NotNull ItemStack offhandItem,
            int materialCost
    ) {
        int amountRemoved = 0;
        for (ItemStack slotItem : inventory.items) {
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
            offhandItem.setCount(offhandItem.getCount() - amountUsed);
        }
        
    }
}
