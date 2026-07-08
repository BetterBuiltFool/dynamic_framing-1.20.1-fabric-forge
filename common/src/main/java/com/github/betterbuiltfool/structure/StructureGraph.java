package com.github.betterbuiltfool.structure;

import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class StructureGraph {
    private final Long2ObjectMap<LongSet> chunkMap = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Node> posToNodeMap = new Long2ObjectOpenHashMap<>();
    
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
    
    /**
     * Creates a map of node positions and their connections from a set of packed positions
     * @param nodePos A LongSet of packed positions whose connections we're after
     * @return A fastutil map representing the nodes and their connections
     */
    public Long2ObjectMap<LongSet> getNodeMap(LongSet nodePos) {
        var subMap = posToNodeMap.values()
                                 .stream()
                                 .filter(x -> nodePos.contains(x.getPos()))
                                 .collect(Collectors.toMap(Node::getPos, Node::getConnections));
        return new Long2ObjectOpenHashMap<>(subMap);
    }
    
    /**
     * Creates a map of node positions and their connections from an array of packed positions
     * @param nodePos An array of packed positions whose connections we're after
     * @return A fastutil map representing the nodes and their connections
     */
    public Long2ObjectMap<LongSet> getNodeMap(long... nodePos) {
        return getNodeMap(new LongOpenHashSet(nodePos));
    }
    
    public void clearAll() {
        posToNodeMap.clear();
        chunkMap.clear();
    }
    
    public void connect(long first, long second) {
        Node start = getOrCreateNode(first);
        Node end = getOrCreateNode(second);
        start.connect(end);
        end.connect(start);
    }
    
    public Node getOrCreateNode(long position) {
        Node fetchedNode = posToNodeMap.get(position);
        if (fetchedNode == null) {
            fetchedNode = new Node(position);
            posToNodeMap.put(position, fetchedNode);
            ChunkPos chunkPos = new ChunkPos(BlockPos.of(position));
            LongSet chunkNodes = chunkMap.computeIfAbsent(chunkPos.toLong(), chunk -> new LongOpenHashSet());
            chunkNodes.add(position);
        }
        return fetchedNode;
    }
    
    //region Serialization
    public CompoundTag serialize(CompoundTag nbt) {
        var nodes = new ListTag();
        for (var node:posToNodeMap.values()) {
            nodes.add(node.serialize(new CompoundTag()));
        }
        nbt.put("nodes", nodes);
        
        return nbt;
    }
    
    public void deserialize(CompoundTag nbt) {
        clearAll();
        var nodes = nbt.getList("nodes", Tag.TAG_COMPOUND);
        for (var nodeData:nodes) {
            var node = Node.deserialize((CompoundTag) nodeData);
            ChunkPos chunkPos = new ChunkPos(node.getBlockPos());
            LongSet chunkNodes = chunkMap.computeIfAbsent(chunkPos.toLong(), key -> new LongOpenHashSet());
            chunkNodes.add(node.getPos());
            this.posToNodeMap.put(node.getPos(), node);
        }
    }
    
    //endregion
}
