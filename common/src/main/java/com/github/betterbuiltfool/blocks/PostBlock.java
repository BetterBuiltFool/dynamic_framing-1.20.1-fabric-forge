package com.github.betterbuiltfool.blocks;

import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import com.github.betterbuiltfool.registry.BlockEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PostBlock extends FrameBlock {
    public static final String BLOCK_ID = "post_block";
    
    public PostBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos,
                                                BlockState state
    ) {
        return new StructureMemberBlockEntity(BlockEntityRegistry.MEMBER_ENTITY.get(), pos, state);
    }
}
