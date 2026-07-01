package com.github.betterbuiltfool.structure;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;

public final class StructureGraph {
    private final ObjectArrayList<Node> levelNodes = new ObjectArrayList<>();
    private final Long2ObjectMap<LongSet> chunkMap = new Long2ObjectOpenHashMap<>();
    
    /**
     * Finds the set of packed node positions that exist within the provided chunks. They will be collected into a
     * single set. If no valid positions exist, the set will be empty.
     * The empty set is read-only, and updating it will not result in the values being associated with that ChunkPos.
     *
     * @param packedChunkPos Any number of longs representing chunk positions.
     * @return A Set of longs, empty if no nodes are found.
     */
    public LongSet getPackedNodesForChunk(long... packedChunkPos) {
        LongSet allNodes = new LongOpenHashSet();
        for (long pos:packedChunkPos) {
            LongSet nodes = chunkMap.getOrDefault(pos, LongSets.EMPTY_SET);
            allNodes.addAll(nodes);
        }
        return allNodes;
    }
    
    /**
     * Finds the set of packed node positions that exist within the provided chunks. They will be collected into a
     * single set. If no valid positions exist, the set will be empty.
     * The empty set is read-only, and updating it will not result in the values being associated with that ChunkPos.
     *
     * @param chunkPos Any number of chunk positions.
     * @return A Set of longs, empty if no nodes are found.
     */
    public LongSet getPackedNodesForChunk(ChunkPos... chunkPos) {
        long[] pos = Arrays.stream(chunkPos)
                           .mapToLong(ChunkPos::toLong)
                           .toArray();
        
        return getPackedNodesForChunk(pos);
    }
    
    public void clearAll() {
        levelNodes.clear();
        chunkMap.clear();
    }
    
    //region Serialization
    public CompoundTag serialize(CompoundTag nbt) {
        var nodes = new ListTag();
        for (var node:levelNodes) {
            nodes.add(node.serialize(new CompoundTag()));
        }
        nbt.put("nodes", nodes);
        
        return nbt;
    }
    
    public void deserialize(CompoundTag nbt) {
        clearAll();
        var nodes = nbt.getList("nodes", Tag.TAG_COMPOUND);
        for (var nodeData:nodes) {
            this.levelNodes.add(Node.deserialize((CompoundTag) nodeData));
        }
    }
    
    //endregion
}
