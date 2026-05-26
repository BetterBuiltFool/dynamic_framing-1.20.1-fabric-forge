package com.github.betterbuiltfool.validation;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class BlockPosValidator {
    
    private static final HashSet<TagKey<Block>> edgeReplacementWhitelist = new HashSet<>();
    
    static {
        // TODO: read this in from config
        edgeReplacementWhitelist.add(BlockTags.REPLACEABLE);
    }
    
    /**
     * Determines if the given BlockPos is able to be replaced with a structure node.
     *
     * @param level The game world
     * @param blockPos The BlockPos to be investigated.
     * @return True if responds to tags, else false
     */
    public static boolean validateEdgePlacement(
            @NotNull Level level,
            @NotNull BlockPos blockPos
    ) {
        var blockState = level.getBlockState(blockPos);
        
        for (TagKey<Block> block: edgeReplacementWhitelist) {
            if (blockState.is(block)) {
                return true;
            }
        }
        
        return blockState.isAir();
    }
}
