package com.github.betterbuiltfool.structure;

import com.github.betterbuiltfool.DynamicFraming;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GraphTargetNbtData {
    private static final String DATA_KEY;
    private static final String DIST_KEY;
    
    static {
        DATA_KEY = DynamicFraming.MOD_ID + ":node_hit_data";
        DIST_KEY = DynamicFraming.MOD_ID + ":node_hit_dist";
    }
    
    public static void saveGraphHit(
            ItemStack stack,
            @Nullable GraphHit graphHit
    ) {
        var nbt = stack.getOrCreateTag();
        
        if (graphHit == null) {
            nbt.remove(DATA_KEY);
            nbt.remove(DIST_KEY);
            return;
        }
        
        long[] data;
        
        if (graphHit instanceof GraphHit.NodeHit nodeHit) {
            data = new long[]{nodeHit.packedPos()};
        } else if (graphHit instanceof GraphHit.EdgeHit edgeHit) {
            data = new long[]{edgeHit.posA(), edgeHit.posB()};
        } else {
            throw new IllegalArgumentException("Invalid GraphHit type!");
        }
        
        nbt.putLongArray(DATA_KEY, data);
        nbt.putDouble(DIST_KEY, graphHit.distance());
    }
    
    @Nullable
    public static GraphHit loadGraphHit(
            ItemStack stack
    ) {
        if (!stack.hasTag()) {
            return null;
        }
        var nbt = stack.getTag();
        if (nbt == null || !nbt.contains(DATA_KEY)) {
            return null;
        }
        
        var data = nbt.getLongArray(DATA_KEY);
        var dist = nbt.getDouble(DIST_KEY);
        
        if (data.length == 1) {
            return new GraphHit.NodeHit(data[0], dist);
        } else {
            return new GraphHit.EdgeHit(data[0], data[1], dist);
        }
    }
}
