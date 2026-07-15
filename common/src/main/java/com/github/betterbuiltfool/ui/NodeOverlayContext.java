package com.github.betterbuiltfool.ui;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class NodeOverlayContext {
    public Minecraft client;
    public PoseStack poseStack;
    public @Nullable NodeOverlayContext.Edge highlightEdge;
    public Long2ObjectMap<LongSet> nodeMap;
    public LongSet highlightNodes;
    public Color highlightColor;
    
    public NodeOverlayContext(
            Minecraft client,
            PoseStack poseStack,
            Long2ObjectMap<LongSet> nodeMap,
            @Nullable NodeOverlayContext.Edge highlightEdge,
            LongSet highlightNodes,
            Color highlightColor
    ) {
        this.client = client;
        this.poseStack = poseStack;
        this.highlightEdge = highlightEdge;
        this.nodeMap = nodeMap;
        this.highlightNodes = highlightNodes;
        this.highlightColor = highlightColor;
    }
    
    public record Edge(long firstPos, long secondPos) {}
}
