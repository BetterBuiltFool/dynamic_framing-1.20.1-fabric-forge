package com.github.betterbuiltfool.data;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.structure.Edge;
import com.github.betterbuiltfool.structure.Node;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class StructureGraph {
    private final Long2ObjectMap<LongSet> chunkMap = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Node> posToNodeMap = new Long2ObjectOpenHashMap<>();
    private final ObjectSet<Edge> activeEdges = new ObjectOpenHashSet<>();
    
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
    public NodeMap getNodeMap(LongSet nodePos) {
        var subMap = posToNodeMap.values()
                                 .stream()
                                 .filter(x -> nodePos.contains(x.getPos()))
                                 .collect(Collectors.toMap(Node::getPos, Node::getConnections));
        return new NodeMap(subMap);
    }
    
    /**
     * Creates a map of node positions and their connections from an array of packed positions
     * @param nodePos An array of packed positions whose connections we're after
     * @return A fastutil map representing the nodes and their connections
     */
    public NodeMap getNodeMap(long... nodePos) {
        return getNodeMap(new LongOpenHashSet(nodePos));
    }
    
    public void clearAll() {
        posToNodeMap.clear();
        chunkMap.clear();
    }
    
    public void remove(long pos) {
        var node = posToNodeMap.getOrDefault(pos, null);
        if (node == null) {
            return;
        }
        for (long connection : node.getConnections()) {
            var connectedNode = posToNodeMap.getOrDefault(connection, null);
            if (connectedNode == null) {
                return;
            }
            connectedNode.getConnections()
                         .remove(pos);
        }
        removeNode(node);
    }
    
    public void remove(long posA,
                       long posB
    ) {
        var nodeA = posToNodeMap.get(posA);
        if (nodeA != null) {
            removeConnection(nodeA, posB);
        }
        var nodeB = posToNodeMap.get(posB);
        if (nodeB != null) {
            removeConnection(nodeB, posA);
        }
    }
    
    public void remove(Edge edge) {
        remove(edge.firstPos(), edge.secondPos());
    }
    
    public void removeNode(@NotNull Node node) {
        posToNodeMap.remove(node.getPos());
        var chunkNodes = chunkMap.getOrDefault(new ChunkPos(node.getBlockPos()).toLong(), LongSets.EMPTY_SET);
        chunkNodes.remove(node.getPos());
        activeEdges.removeIf(edge -> edge.firstPos() == node.getPos() || edge.secondPos() == node.getPos());
    }
    
    public void removeEdge(@NotNull Edge edge) {
        activeEdges.remove(edge);
        
        cleanupEdgeNodes(edge.firstPos(), edge.secondPos());
        cleanupEdgeNodes(edge.secondPos(), edge.firstPos());
    }
    
    private void cleanupEdgeNodes(long firstPos,
                                  long secondPos
    ) {
        Node first = posToNodeMap.get(firstPos);
        if (first == null) {
            return;
        }
        var connections = first.getConnections();
        connections.remove(secondPos);
    }
    
    private void removeConnection(@NotNull Node node,
                                  long connectedPos
    ) {
        var connections = node.getConnections();
        connections.remove(connectedPos);
        if (connections.isEmpty()) {
            removeNode(node);
        } else if (connections.size() == 2) {
            if (tryMergeEdges(node.getEdges())) {
                removeNode(node);
            }
        }
    }
    
    private boolean tryMergeEdges(Set<Edge> edges) {
        var iterator = edges.iterator();
        var edge1 = iterator.next();
        var edge2 = iterator.next();
        if (edge1.axis() != edge2.axis()) {
            return false;
        }
        removeEdge(edge1);
        removeEdge(edge2);
        
        var sharedPos = edge1.getSharedEnd(edge2);
        long newStart = edge1.getOpposingEnd(sharedPos);
        long newEnd = edge2.getOpposingEnd(sharedPos);
        
        insertEdge(new Edge(newStart, newEnd));
        return true;
    }
    
    public void connect(long first, long second) {
        var firstEdges = getLappingEdge(first);
        var secondEdges = getLappingEdge(second);
        
        var overlap = new HashSet<>(firstEdges);
        overlap.retainAll(secondEdges);
        if (!overlap.isEmpty()) {
            DynamicFraming.LOGGER.info("New edge would be a segment of existing edge, aborting.");
            return;
        }
        first = updateEdge(first, second, firstEdges);
        second = updateEdge(second, first, secondEdges);
        
        Edge edge = new Edge(first, second);
        // TODO: Handle overlaps
        insertEdge(edge);
    }
    
    /**
     * Handles any edges where the firstPos intersects. Coaxial edges are check for extendability, perpendicular edges
     * for splitting needs.
     * If an existing edge will be extended, the firstPos value will be adjusted to cover the new edge.
     *
     * @param firstPos  A packed position that rests on the edges.
     * @param secondPos A packed position on the opposite end of the new edge from firstPos
     * @param edges     The set of edges that intersect with firstPos
     *
     * @return An updated packed long where firstPos should be after modifying the grid.
     */
    private long updateEdge(long firstPos,
                            long secondPos,
                            Set<Edge> edges
    ) {
        var partitioned = edges.stream()
                               .collect(Collectors.partitioningBy(
                                       edge -> edge.isCoaxialTo(secondPos),
                                       Collectors.toSet()
                               ));
        var coaxialEdges = partitioned.get(true);
        var perpendicularEdges = partitioned.get(false);
        
        if (!coaxialEdges.isEmpty()) {
            Edge extendingEdge;
            if (coaxialEdges.size() == 1) {
                extendingEdge = coaxialEdges.iterator()
                                            .next();
            } else {
                extendingEdge = edges.stream()
                                     .min(Comparator.comparingLong(
                                             edge -> Edge.distanceSqr(edge.getClosestEnd(secondPos), secondPos)
                                     ))
                                     .orElseThrow();
            }
            long closestEnd = extendingEdge.getClosestEnd(secondPos);
            if (posToNodeMap.get(closestEnd)
                            .getConnections()
                            .size() > 1) {
                return closestEnd;
            }
            remove(extendingEdge);
            
            return extendingEdge.getOpposingEnd(closestEnd);
        }
        
        for (var edge : perpendicularEdges) {
            if (edge.firstPos() == firstPos || edge.secondPos() == firstPos) {
                continue;
            }
            var split = edge.splitAt(firstPos);
            remove(edge);
            insertEdge(split.upper());
            insertEdge(split.lower());
        }
        
        return firstPos;
    }
    
    private void insertEdge(Edge edge) {
        
        Node first = getOrCreateNode(edge.firstPos());
        Node second = getOrCreateNode(edge.secondPos());
        
        first.connect(second);
        second.connect(first);
        activeEdges.add(edge);
    }
    
    private Set<Edge> getLappingEdge(long position) {
        if (posToNodeMap.containsKey(position)) {
            return posToNodeMap.get(position)
                               .getEdges();
        }
        
        return activeEdges.stream()
                          .filter(edge -> edge.isCoaxialTo(position))
                          .filter(edge -> edge.intersectedBy(position))
                          .collect(Collectors.toSet());
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
            for (var connection : node.getConnections()) {
                if (node.getPos() < connection) {
                    this.activeEdges.add(new Edge(node.getPos(), connection));
                }
            }
        }
    }
    
    //endregion
}
