package com.github.betterbuiltfool.client;

import com.github.betterbuiltfool.blocks.block_entities.Alignment;
import com.github.betterbuiltfool.blocks.block_entities.Size;
import net.minecraft.world.level.block.state.BlockState;

public record CacheKey(
        BlockState material,
        Size size,
        Alignment x,
        Alignment y,
        Alignment z
) {
}
