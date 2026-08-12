package com.github.betterbuiltfool.blocks.block_entities;

import com.github.betterbuiltfool.blocks.FrameBlockStateData;
import com.github.betterbuiltfool.data.CoaxSelection;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class StructureJointBlockEntity extends BlockEntity {
    
    private Alignment alignX = Alignment.CENTER;
    private Alignment alignY = Alignment.CENTER;
    private Alignment alignZ = Alignment.CENTER;
    
    private final Long2ObjectMap<EdgeProfile> edges = new Long2ObjectOpenHashMap<>();
    private final Object2LongMap<Direction> connections = new Object2LongOpenHashMap<>();
    
    public StructureJointBlockEntity(BlockEntityType<?> type,
                                     BlockPos pos,
                                     BlockState blockState
    ) {
        super(type, pos, blockState);
    }
    
    //region Mutators
    
    public void setJointAlignment(Alignment x,
                                  Alignment y,
                                  Alignment z
    ) {
        this.alignX = x;
        this.alignY = y;
        this.alignZ = z;
        this.sync();
    }
    
    public void registerConnection(BlockPos position,
                                   BlockState material,
                                   Size size
    ) {
        var directionVector = position.subtract(this.worldPosition);
        var facing = Direction.getNearest(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
        edges.put(position.asLong(), new EdgeProfile(material, size, facing));
        connections.put(facing, position.asLong());
        sync();
    }
    
    //endregion
    //region Accessors
    
    public Alignment getAlignX() {
        return alignX;
    }
    
    public Alignment getAlignY() {
        return alignY;
    }
    
    public Alignment getAlignZ() {
        return alignZ;
    }
    
    public EdgeProfile getEdgeProfile(BlockPos position) {
        return edges.get(position.asLong());
    }
    
    public FrameBlockStateData getEdgeData(Direction direction) {
        long connection = this.connections.getLong(direction);
        var edgeProfile = this.edges.get(connection);
        
        return new FrameBlockStateData(alignX, alignY, alignZ, edgeProfile.size());
    }
    
    //endregion
    //region Mutators
    public void pushAxis(Direction.Axis axis) {
        
        assert this.level != null;
        
        LongSet visited = new LongOpenHashSet();
        LongArrayFIFOQueue toProcess = new LongArrayFIFOQueue();
        
        long thisPos = this.worldPosition.asLong();
        visited.add(thisPos);
        toProcess.enqueue(thisPos);
        
        while (!toProcess.isEmpty()) {
            var pos = toProcess.dequeueLong();
            if (visited.contains(pos)) {
                continue;
            }
            if (!CoaxSelection.isCoplanar(thisPos, pos, axis)) {
                continue;
            }
            var blockEntity = this.level.getBlockEntity(BlockPos.of(pos));
            if (!(blockEntity instanceof StructureJointBlockEntity structureJointBlockEntity)) {
                continue;
            }
            for (long connectedPos : structureJointBlockEntity.edges.keySet()) {
                toProcess.enqueue(connectedPos);
            }
            structureJointBlockEntity.push(axis);
        }
    }
    
    public void push(Direction.Axis axis) {
        switch (axis) {
            case X -> alignX = alignX.push();
            case Y -> alignY = alignY.push();
            case Z -> alignZ = alignZ.push();
        }
        sync();
    }
    //endregion
    
    private void sync() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }
    
    //region Serialization
    
    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("JointAX", alignX.ordinal());
        tag.putInt("JointAY", alignY.ordinal());
        tag.putInt("JointAZ", alignZ.ordinal());
        
        ListTag list = new ListTag();
        for (var entry : edges.long2ObjectEntrySet()) {
            CompoundTag entryTag = new CompoundTag();
            var profile = entry.getValue();
            entryTag.putLong("TargetNodePos", entry.getLongKey());
            entryTag.put("Material", NbtUtils.writeBlockState(profile.material()));
            entryTag.putInt("Size", profile.size()
                                           .ordinal()
            );
        }
        tag.put("Edges", list);
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.alignX = Alignment.values()[tag.getInt("JointAX")];
        this.alignY = Alignment.values()[tag.getInt("JointAY")];
        this.alignZ = Alignment.values()[tag.getInt("JointAZ")];
        
        edges.clear();
        if (!tag.contains("Edges", 9)) {
            return;
        }
        var list = tag.getList("Edges", 10);
        var blockLookup = BuiltInRegistries.BLOCK.asLookup();
        
        for (int i = 0; i < list.size(); i++) {
            var entry = list.getCompound(i);
            long nodePos = entry.getLong("TargetNodePos");
            BlockState material = NbtUtils.readBlockState(blockLookup, entry.getCompound("Material"));
            Size size = Size.values()[entry.getInt("Size")];
            
            var directionVector = BlockPos.of(nodePos)
                                          .subtract(this.worldPosition);
            var facing = Direction.getNearest(
                    directionVector.getX(),
                    directionVector.getY(),
                    directionVector.getZ()
            );
            
            edges.put(nodePos, new EdgeProfile(material, size, facing));
        }
    }
    //endregion
}
