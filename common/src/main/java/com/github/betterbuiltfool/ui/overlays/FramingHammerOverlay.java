package com.github.betterbuiltfool.ui.overlays;

import com.github.betterbuiltfool.ui.LineRenderer;
import com.github.betterbuiltfool.ui.NodeMarkerRenderer;
import com.github.betterbuiltfool.ui.NodeOverlayContext;

import java.awt.*;

public class FramingHammerOverlay {
    private static final Color defaultColor = new Color(0, 0, 255);
    
    public static void renderOverlay(NodeOverlayContext context) {
        renderEdges(context);
        renderNodes(context);
    }
    
    private static void renderNodes(NodeOverlayContext context) {
        var positions = context.highlightNodes;
        var nodeRenderer = new NodeMarkerRenderer(context.client, context.poseStack);
        
        nodeRenderer.drawBatch(renderer -> {
            for (long node : positions) {
                renderer.renderNode(node);
            }
        });
    }
    
    public static void renderEdges(NodeOverlayContext context) {
        var nodeMap = context.nodeMap;
        var highlightEdge = context.highlightEdge;
        var lineRenderer = new LineRenderer(context.client, context.poseStack);
        
        boolean hasHighlightEdge = (highlightEdge != null);
        long firstHighlightPoint;
        long secondHighlightPoint;
        
        if (hasHighlightEdge) {
            firstHighlightPoint = Math.min(highlightEdge.firstPos(), highlightEdge.secondPos());
            secondHighlightPoint = Math.max(highlightEdge.firstPos(), highlightEdge.secondPos());
        } else {
            secondHighlightPoint = 0;
            firstHighlightPoint = 0;
        }
        
        lineRenderer.drawBatch(renderer -> {
            if (nodeMap != null) {
                for (var entry : nodeMap.entrySet()) {
                    long node = entry.getLongKey();
                    
                    for (long connection : entry.getValue()) {
                        if (node < connection || !nodeMap.containsNode(connection)) {
                            var lineColor = defaultColor;
                            if (hasHighlightEdge && node == firstHighlightPoint && connection == secondHighlightPoint) {
                                continue;
                            }
                            lineRenderer.renderLine(node, connection, lineColor);
                        }
                    }
                }
            }
            lineRenderer.renderLine(firstHighlightPoint, secondHighlightPoint, context.highlightColor);
        });
    }
}
