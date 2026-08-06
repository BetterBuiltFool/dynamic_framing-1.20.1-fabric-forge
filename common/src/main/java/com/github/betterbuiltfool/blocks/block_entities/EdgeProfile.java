package com.github.betterbuiltfool.blocks.block_entities;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record EdgeProfile(BlockState material, Size size, Direction direction) {
}
