package com.github.betterbuiltfool.config;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ConfigData (
        Integer lineColor,
        Integer invalidEdgeColor,
        Integer validEdgeColor,
        Integer selectionColor,
        Integer removeSelectionColor,
        List<String> blockReplaceWhiteList
) {
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color SKY_BLUE = new Color(0, 192, 255);
    public static final Color DARK_ORANGE = new Color(255, 127, 0);
    
    public ConfigData {
        lineColor = Objects.requireNonNullElse(lineColor, BLUE.getRGB());
        invalidEdgeColor = Objects.requireNonNullElse(invalidEdgeColor, RED.getRGB());
        validEdgeColor = Objects.requireNonNullElse(validEdgeColor, GREEN.getRGB());
        selectionColor = Objects.requireNonNullElse(selectionColor, SKY_BLUE.getRGB());
        removeSelectionColor = Objects.requireNonNullElse(removeSelectionColor, DARK_ORANGE.getRGB());
        blockReplaceWhiteList = Objects.requireNonNullElse(blockReplaceWhiteList, new ArrayList<>());
    }
    public ConfigData (){
        this(null, null , null, null, null, null);
    }
}
