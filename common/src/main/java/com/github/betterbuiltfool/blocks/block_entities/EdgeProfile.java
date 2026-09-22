package com.github.betterbuiltfool.blocks.block_entities;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public record EdgeProfile(BlockState material, Size size, Direction direction) {
    
    public EdgeProfile setSize(Size size) {
        return new EdgeProfile(this.material, size, this.direction);
    }
}
