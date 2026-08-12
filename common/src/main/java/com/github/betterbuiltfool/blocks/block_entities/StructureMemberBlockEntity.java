package com.github.betterbuiltfool.blocks.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StructureMemberBlockEntity extends BlockEntity {
    
    public Direction direction;
    public BlockPos jointPos;
    
    public StructureMemberBlockEntity(BlockEntityType<?> type,
                                      BlockPos pos,
                                      BlockState blockState
    ) {
        super(type, pos, blockState);
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
    }
    
    public void setJointPos(BlockPos jointPos) {
        this.jointPos = jointPos;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public BlockPos getJointPos() {
        return jointPos;
    }
    
    //region Serialization
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        NbtUtils.writeBlockPos(jointPos);
        tag.putString("facing", direction.getName());
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        jointPos = NbtUtils.readBlockPos(tag);
        direction = Direction.byName(tag.getString("facing"));
    }
    
    //endregion
}
