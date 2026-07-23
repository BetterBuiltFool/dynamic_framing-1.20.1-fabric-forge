package com.github.betterbuiltfool.data;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.structure.GraphHit;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class GraphHitNbtData {
    private static final String DATA_KEY;
    private static final String DIST_KEY;
    
    static {
        DATA_KEY = DynamicFraming.MOD_ID + ":node_hit_data";
        DIST_KEY = DynamicFraming.MOD_ID + ":node_hit_dist";
    }
    
    public static void saveGraphHit(
            CompoundTag nbt,
            @Nullable GraphHit graphHit
    ) {
        if (graphHit == null) {
            nbt.remove(DATA_KEY);
            nbt.remove(DIST_KEY);
            return;
        }
        
        long[] data;
        
        if (graphHit instanceof GraphHit.NodeHit nodeHit) {
            data = new long[]{nodeHit.packedPos()};
        } else if (graphHit instanceof GraphHit.EdgeHit edgeHit) {
            data = new long[]{edgeHit.posA(), edgeHit.posB(), edgeHit.hitPos()};
        } else {
            throw new IllegalArgumentException("Invalid GraphHit type!");
        }
        
        nbt.putLongArray(DATA_KEY, data);
        nbt.putDouble(DIST_KEY, graphHit.distance());
    }
    
    @Nullable
    public static GraphHit loadGraphHit(
            CompoundTag nbt
    ) {
        if (nbt == null || !nbt.contains(DATA_KEY)) {
            return null;
        }
        
        var data = nbt.getLongArray(DATA_KEY);
        var dist = nbt.getDouble(DIST_KEY);
        
        if (data.length == 1) {
            return new GraphHit.NodeHit(data[0], dist);
        } else {
            return new GraphHit.EdgeHit(data[0], data[1], data[2], dist);
        }
    }
}
