package com.github.betterbuiltfool.data;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.network.ChunkNodeDataPacket;
import com.github.betterbuiltfool.network.DynamicFramingNetworking;
import com.github.betterbuiltfool.structure.NodeMap;
import com.github.betterbuiltfool.structure.StructureGraph;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FramedStructureStorage extends SavedData {
    private final Map<ResourceKey<Level>, StructureGraph> dimensionGraphs;
    private static final String DATA_ID;
    
    static {
        DATA_ID = DynamicFraming.MOD_ID + "_data";
    }
    
    public FramedStructureStorage() {
        dimensionGraphs = new HashMap<>();
    }
    
    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag) {
        CompoundTag structureData = new CompoundTag();
        for (var entry:dimensionGraphs.entrySet()) {
            var dimensionKey = entry.getKey();
            var structureGraph = entry.getValue();
            
            String dimension = dimensionKey.location().toString();
            
            CompoundTag nbt = new CompoundTag();
            
            structureData.put(dimension, structureGraph.serialize(nbt));
        }
        compoundTag.put(DATA_ID, structureData);
        return compoundTag;
    }
    
    public static FramedStructureStorage load(CompoundTag compoundTag) {
        FramedStructureStorage storage = new FramedStructureStorage();
        CompoundTag structureData = compoundTag.getCompound(DATA_ID);
        
        for (String key:structureData.getAllKeys()) {
            // TODO: Add error handling for tryParse failing, this could cause issues if a dimension mod is removed after using.
            ResourceLocation dimensionLocation = ResourceLocation.tryParse(key);
            ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, dimensionLocation);
            
            Tag data = structureData.get(key);
            
            assert data != null;
            
            StructureGraph levelGraph = new StructureGraph();
            levelGraph.deserialize((CompoundTag) data);
            
            storage.dimensionGraphs.put(dimensionKey, levelGraph);
        }
        return storage;
    }
    
    public static FramedStructureStorage get(Level level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                FramedStructureStorage::load,
                FramedStructureStorage::new,
                DATA_ID
        );
    }
    
    public StructureGraph getDimensionGraph(ResourceKey<Level> dimensionKey) {
        return dimensionGraphs.get(dimensionKey);
    }
    
    public StructureGraph getOrCreateDimensionGraph(ResourceKey<Level> dimensionKey) {
        return dimensionGraphs.computeIfAbsent(dimensionKey, key -> new StructureGraph());
    }
    
    public static StructureGraph getOrCreateDimensionGraph(Level level) {
        var storage = FramedStructureStorage.get(level);
        return storage.getOrCreateDimensionGraph(level.dimension());
    }
    
    public static void clearAll(Level level) {
        var storage = FramedStructureStorage.get(level);
        storage.dimensionGraphs.clear();
        storage.setDirty();
    }
    
    public static void sendChunkDataToPlayer(ServerPlayer player, ResourceKey<Level> dimension, long... pos) {
        Level serverLevel = player.serverLevel();
        FramedStructureStorage storage = FramedStructureStorage.get(serverLevel);
        StructureGraph dimensionGraph = storage.getDimensionGraph(serverLevel.dimension());
        
        LongSet nodesPositions = dimensionGraph.getPackedNodesForChunk(pos);
        NodeMap nodeData = dimensionGraph.getNodeMap(nodesPositions);
        DynamicFramingNetworking.CHANNEL.sendToPlayer(player, new ChunkNodeDataPacket(nodeData));
    }
    
    public static ChunkPos[] getSurroundingChunks(ChunkPos center) {
        return ChunkPos.rangeClosed(
                               center,
                               1
                       )
                       .toArray(ChunkPos[]::new);
    }
}
