package com.github.betterbuiltfool.ui.overlays;

import com.github.betterbuiltfool.ui.NodeOverlayContext;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

public class NodeOverlayContextBuilder {
    public final Long2ObjectMap<LongSet> nodeMap = new Long2ObjectOpenHashMap<>();
    private final Minecraft client;
    private final PoseStack poseStack;
    private final Object2LongMap<String> highlightEdgePositions = new Object2LongOpenHashMap<>();
    private final LongSet highlightNodes = new LongOpenHashSet();
    
    public NodeOverlayContextBuilder(Minecraft client,
                                     PoseStack poseStack
    ) {
        this.client = client;
        this.poseStack = poseStack;
    }
    
    public NodeOverlayContextBuilder addFirstPos(long firstPos) {
        this.highlightEdgePositions.put("first", firstPos);
        return this;
    }
    
    public NodeOverlayContextBuilder addSecondPos(long secondPos) {
        this.highlightEdgePositions.put("second", secondPos);
        return this;
    }
    
    public NodeOverlayContextBuilder addHighlightPos(long nodePos) {
        highlightNodes.add(nodePos);
        return this;
    }
    
    public NodeOverlayContextBuilder addHighlightPos(LongSet nodePoses) {
        highlightNodes.addAll(nodePoses);
        return this;
    }
    
    public NodeOverlayContextBuilder addNodeMap(Long2ObjectMap<LongSet> nodeMap) {
        this.nodeMap.putAll(nodeMap);
        return this;
    }
    
    public NodeOverlayContext build() {
        return new NodeOverlayContext(
                this.client,
                this.poseStack,
                this.nodeMap,
                this.getHighlightEdge(),
                this.highlightNodes
        );
    }
    
    private @Nullable NodeOverlayContext.Edge getHighlightEdge() {
        if (!(highlightEdgePositions.containsKey("first") && highlightEdgePositions.containsKey("second"))) {
            return null;
        }
        var firstPos = highlightEdgePositions.getLong("first");
        var secondPos = highlightEdgePositions.getLong("second");
        
        return new NodeOverlayContext.Edge(firstPos, secondPos);
    }
}
