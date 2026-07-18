package com.github.betterbuiltfool.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.network.FriendlyByteBuf;

public class NodeMap {
    private final Long2ObjectMap<LongSet> graph;
    
    private NodeMap() {
        this.graph = new Long2ObjectOpenHashMap<>();
    }
    
    public static NodeMap decode(FriendlyByteBuf buffer) {
        Long2ObjectMap<LongSet> nodePos = new Long2ObjectOpenHashMap<>();
        var size = buffer.readInt();
        for (int i = 0; i < size; i++) {
            var key = buffer.readLong();
            var values = buffer.readLongArray();
            nodePos.put(key, LongSet.of(values));
        }
        
        var nodeMap = new NodeMap();
        nodeMap.graph.putAll(nodePos);
        return nodeMap;
    }
    
    public void encode(FriendlyByteBuf buffer) {
        int size = this.graph.size();
        buffer.writeInt(size);
        for (var entry : this.graph.long2ObjectEntrySet()) {
            var key = entry.getLongKey();
            var value = entry.getValue();
            buffer.writeLong(key);
            buffer.writeLongArray(value.toLongArray());
        }
    }
    
    public Long2ObjectMap<LongSet> getGraph() {
        return graph;
    }
    
    public ObjectSet<Long2ObjectMap.Entry<LongSet>> getEntrySet() {
        return graph.long2ObjectEntrySet();
    }
}
