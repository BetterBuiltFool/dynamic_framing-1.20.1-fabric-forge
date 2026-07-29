package com.github.betterbuiltfool.validation;

import com.github.betterbuiltfool.config.CommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class BlockPosValidator {
    
    private static final TagKey<Block> ALWAYS_REPLACEABLE = BlockTags.REPLACEABLE;
    /**
     * Determines if the given BlockPos is able to be filled by framing.
     *
     * @param level The game world
     * @param blockPos The BlockPos to be investigated.
     * @return True if responds to tags, else false
     */
    public static boolean validate(
            @NotNull Level level,
            @NotNull BlockPos blockPos
    ) {
        var blockState = level.getBlockState(blockPos);
        
        for (TagKey<Block> block : CommonConfig.blockReplaceWhitelist.tags()) {
            if (blockState.is(block)) {
                return true;
            }
        }
        
        return blockState.isAir() || blockState.is(ALWAYS_REPLACEABLE);
    }
}
