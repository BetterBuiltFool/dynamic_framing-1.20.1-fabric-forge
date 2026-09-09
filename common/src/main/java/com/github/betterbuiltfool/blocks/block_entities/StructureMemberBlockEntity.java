package com.github.betterbuiltfool.blocks.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StructureMemberBlockEntity extends BlockEntity {
    
    public Direction direction;
    public BlockPos jointPos;
    private @Nullable BlockState material;
    
    public StructureMemberBlockEntity(BlockEntityType<?> type,
                                      BlockPos pos,
                                      BlockState blockState
    ) {
        super(type, pos, blockState);
    }
    
    public void setDirection(Direction direction) {
        this.direction = direction;
        setChanged();
    }
    
    public void setJointPos(BlockPos jointPos) {
        this.jointPos = jointPos;
        setChanged();
    }
    
    public void setMaterial(@Nullable BlockState material) {
        this.material = material;
        setChanged();
        requestRenderUpdate();
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public BlockPos getJointPos() {
        return jointPos;
    }
    
    public @Nullable BlockState getMaterial() {
        if (material != null) {
            return material;
        }
        assert level != null;
        if (jointPos == null) return null;
        var be = level.getBlockEntity(jointPos);
        if (be instanceof StructureJointBlockEntity jointEntity) {
            return jointEntity.getEdgeMaterial(direction);
        }
        
        return null;
    }
    
    private void requestRenderUpdate() {
        if (level == null) {
            return;
        }
        var state = getBlockState();
        level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
    }
    
    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        
        if (!level.isClientSide() && material != null) {
            requestRenderUpdate();
        }
    }
    
    //region Serialization
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("joint_pos", NbtUtils.writeBlockPos(jointPos));
        tag.putString("facing", direction.getName());
        if (getMaterial() != null) {
            tag.put("material", NbtUtils.writeBlockState(getMaterial()));
        }
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        var jointPosTag = tag.getCompound("joint_pos");
        var blockLookup = BuiltInRegistries.BLOCK.asLookup();
        jointPos = NbtUtils.readBlockPos(jointPosTag);
        direction = Direction.byName(tag.getString("facing"));
        if (tag.contains("material", Tag.TAG_COMPOUND)) {
            material = NbtUtils.readBlockState(blockLookup, tag.getCompound("material"));
        }
    }
    
    @Override
    public @NotNull CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }
    
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    //endregion
}
