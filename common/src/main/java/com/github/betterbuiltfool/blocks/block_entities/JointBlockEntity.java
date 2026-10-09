package com.github.betterbuiltfool.blocks.block_entities;

import com.github.betterbuiltfool.blocks.BeamBlock;
import com.github.betterbuiltfool.blocks.FrameBlockStateData;
import com.github.betterbuiltfool.blocks.JointBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class JointBlockEntity extends BlockEntity {
    
    private Alignment alignX = Alignment.CENTER;
    private Alignment alignY = Alignment.CENTER;
    private Alignment alignZ = Alignment.CENTER;
    
    private final Long2ObjectMap<EdgeProfile> edges = new Long2ObjectOpenHashMap<>();
    private final Object2LongMap<Direction> connections = new Object2LongOpenHashMap<>();
    
    public JointBlockEntity(BlockEntityType<?> type,
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
    
    public void removeConnection(long position) {
        if (!edges.containsKey(position)) return;
        var edgeProfile = edges.get(position);
        edges.remove(position);
        connections.removeLong(edgeProfile.direction());
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
    
    public int getConnectionCount() {
        return connections.size();
    }
    
    public long getConnection(Direction direction) {
        return connections.getLong(direction);
    }
    
    public boolean hasConnection(Direction direction) {
        return connections.containsKey(direction);
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
    
    public Map<Direction, BlockState> getEdgeMaterials() {
        Map<Direction, BlockState> materials = new HashMap<>();
        
        for (var entrySet : connections.object2LongEntrySet()) {
            var direction = entrySet.getKey();
            var connectionPos = entrySet.getLongValue();
            var edgeProfile = edges.get(connectionPos);
            
            materials.put(direction, edgeProfile != null ? edgeProfile.material(): null);
        }
        return materials;
    }
    
    public RenderData getRenderData() {
        var materials = getEdgeMaterials();
        
        materials.replaceAll((direction, material) ->
                                     material != null ? material : Blocks.OAK_LOG.defaultBlockState()
                                                                                 .setValue(BlockStateProperties.AXIS,
                                                                                           direction.getAxis()
                                                                                 )
        );
        
        return new RenderData(materials);
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
    public void cycleEdgeScale(Direction direction) {
        
        assert this.level != null;
        
        if (!connections.containsKey(direction)) return;
        
        var connectionPos = connections.getLong(direction);
        var edgeProfile = getEdgeProfile(direction);
        
        var size = edgeProfile.size()
                              .cycle();
        
        var newProfile = edgeProfile.setSize(size);
        
        setEdgeProfile(BlockPos.of(connectionPos), newProfile);
        setChanged();
        sync();
        
        var connectedBE = level.getBlockEntity(BlockPos.of(connectionPos));
        if (!(connectedBE instanceof JointBlockEntity jbe)) {
            return;
        }
        
        jbe.setEdgeProfile(this.worldPosition, newProfile);
        jbe.setChanged();
        jbe.sync();
        
    }
    
    public void cycleAxisAlignment(Direction.Axis axis) {
        
        assert this.level != null;
        
        LongSet visited = new LongOpenHashSet();
        LongArrayFIFOQueue toProcess = new LongArrayFIFOQueue();
        
        long thisPos = this.worldPosition.asLong();
        visited.add(thisPos);
        toProcess.enqueue(thisPos);
        
        while (!toProcess.isEmpty()) {
            var pos = toProcess.dequeueLong();
            var blockEntity = this.level.getBlockEntity(BlockPos.of(pos));
            if (!(blockEntity instanceof JointBlockEntity jointBlockEntity)) {
                continue;
            }
            for (long connectedPos : jointBlockEntity.edges.keySet()) {
                if (!CoaxSelection.isCoplanar(thisPos, connectedPos, axis) || visited.contains(connectedPos)) {
                    continue;
                }
                toProcess.enqueue(connectedPos);
            }
            jointBlockEntity.cycleAlignment(axis);
            visited.add(jointBlockEntity.worldPosition.asLong());
        }
        visited.forEach((long jointPos) -> {
            if (!(level.getBlockEntity(BlockPos.of(jointPos)) instanceof JointBlockEntity jbe)) {
                return;
            }
            jbe.sync();
        });
    }
    
    private void cycleAlignment(Direction.Axis axis) {
        switch (axis) {
            case X -> alignX = alignX.cycle();
            case Y -> alignY = alignY.cycle();
            case Z -> alignZ = alignZ.cycle();
        }
    }
    //endregion
    
    private void sync() {
        this.setChanged();
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        for (var direction : this.connections.keySet()) {
            syncEdge(direction);
        }
        var newState = this.getBlockState()
                           .setValue(JointBlock.ALIGNMENT_PRIMARY, this.alignX)
                           .setValue(JointBlock.ALIGNMENT_SECONDARY, this.alignY)
                           .setValue(JointBlock.ALIGNMENT_TERTIARY, this.alignZ);
        
        for (var direction : Direction.values()) {
            newState = setDirectionSize(newState, direction);
        }
        
        this.level.setBlock(
                this.worldPosition,
                newState,
                Block.UPDATE_ALL
        );
    }
    
    public void syncEdge(Direction direction) {
        if (!connections.containsKey(direction)) {
            return;
        }
        
        long packedConnection = connections.getLong(direction);
        var edgeProfile = edges.get(packedConnection);
        var material = edgeProfile.material();
        var connectionPos = BlockPos.of(packedConnection);
        
        BlockPos.betweenClosedStream(this.worldPosition, connectionPos)
                .forEach(pos -> {
                    assert level != null;
                    var state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof BeamBlock)) {
                        return;
                    }
                    var axis = direction.getAxis();
                    Alignment primary;
                    Alignment secondary;
                    switch (axis) {
                        case X -> {
                            primary = this.alignY;
                            secondary = this.alignZ;
                        }
                        case Y -> {
                            primary = this.alignX;
                            secondary = this.alignZ;
                        }
                        default -> {
                            primary = this.alignX;
                            secondary = this.alignY;
                        }
                    }
                    var newState = state.setValue(BeamBlock.ALIGNMENT_PRIMARY, primary)
                                        .setValue(BeamBlock.ALIGNMENT_SECONDARY, secondary)
                                        .setValue(BeamBlock.SCALING, edgeProfile.size());
                    
                    level.setBlock(pos, newState, Block.UPDATE_CLIENTS);
                    
                    if (!(level.getBlockEntity(pos) instanceof BeamBlockEntity be)) {
                        return;
                    }
                    be.setMaterial(material);
                    be.setChanged();
                });
        
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
    
    private BlockState setDirectionSize(BlockState initial,
                                        Direction direction
    ) {
        var edgeProfile = getEdgeProfile(direction);
        Size size;
        if (edgeProfile != null) {
            size = edgeProfile.size();
        } else {
            size = Size.NONE;
        }
        var property = JointBlock.connectionProperties.get(direction);
        return initial.setValue(property, size);
    }
    
    public record RenderData(Map<Direction, BlockState> renderMap) {}
}
