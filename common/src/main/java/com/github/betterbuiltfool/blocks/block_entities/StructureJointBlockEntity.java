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
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    
    public EdgeProfile getEdgeProfile(BlockPos connectedPos) {
        return edges.get(connectedPos.asLong());
    }
    
    public EdgeProfile getEdgeProfile(Direction direction) {
        return getEdgeProfile(BlockPos.of(this.connections.getLong(direction)));
    }
    
    public FrameBlockStateData getEdgeData(Direction direction) {
        var edgeProfile = getEdgeProfile(direction);
        
        return new FrameBlockStateData(alignX, alignY, alignZ, edgeProfile.size());
    }
    
    public BlockState getEdgeMaterial(Direction direction) {
        var edgeProfile = getEdgeProfile(direction);
        return edgeProfile != null ? edgeProfile.material(): null;
    }
    
    public void setEdgeProfile(BlockPos connectedPos,
                               EdgeProfile profile
    ) {
        edges.put(connectedPos.asLong(), profile);
    }
    
    public void setEdgeProfile(Direction direction,
                               EdgeProfile profile
    ) {
        edges.put(connections.getLong(direction), profile);
    }
    
    public void setEdgeData(Direction direction, FrameBlockStateData data) {
        var edgeProfile = getEdgeProfile(direction);
        
        alignX = data.alignX();
        alignY = data.alignY();
        alignZ = data.alignZ();
        setEdgeProfile(direction, new EdgeProfile(edgeProfile.material(), data.size(), direction));
    }
    
    public void setEdgeMaterial(Direction direction, BlockState material) {
        var edgeProfile = getEdgeProfile(direction);
        setEdgeProfile(direction, new EdgeProfile(material, edgeProfile.size(), direction));
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
        
        ListTag edgeTagList = new ListTag();
        for (var entry : edges.long2ObjectEntrySet()) {
            CompoundTag entryTag = new CompoundTag();
            var profile = entry.getValue();
            entryTag.putLong("TargetNodePos", entry.getLongKey());
            entryTag.put("Material", NbtUtils.writeBlockState(profile.material()));
            entryTag.putInt("Size", profile.size()
                                           .ordinal()
            );
            
            edgeTagList.add(entryTag);
        }
        tag.put("Edges", edgeTagList);
        
        ListTag connectionsTagList = new ListTag();
        for (var entry : connections.object2LongEntrySet()) {
            var key = entry.getKey();
            var value = entry.getLongValue();
            
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("direction", key.getName());
            entryTag.putLong("connection", value);
            
            connectionsTagList.add(entryTag);
        }
        
        tag.put("Connections", connectionsTagList);
        
    }
    
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.alignX = Alignment.values()[tag.getInt("JointAX")];
        this.alignY = Alignment.values()[tag.getInt("JointAY")];
        this.alignZ = Alignment.values()[tag.getInt("JointAZ")];
        
        edges.clear();
        connections.clear();
        if (!tag.contains("Edges", Tag.TAG_LIST)) {
            return;
        }
        var edgeTagList = tag.getList("Edges", Tag.TAG_COMPOUND);
        var blockLookup = BuiltInRegistries.BLOCK.asLookup();
        
        for (int i = 0; i < edgeTagList.size(); i++) {
            var entry = edgeTagList.getCompound(i);
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
        if (!tag.contains("Connections", Tag.TAG_LIST)) {
            return;
        }
        
        var connectionTagList = tag.getList("Connections", Tag.TAG_COMPOUND);
        
        
        for (int i = 0; i < connectionTagList.size(); i++) {
            var entry = connectionTagList.getCompound(i);
            long connection = entry.getLong("connection");
            Direction facing = Direction.byName(entry.getString("direction"));
            
            connections.put(facing, connection);
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
