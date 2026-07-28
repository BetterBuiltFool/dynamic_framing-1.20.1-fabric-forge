package com.github.betterbuiltfool.config;

import java.awt.*;

public record ConfigData (
        int lineColor,
        int invalidEdgeColor,
        int validEdgeColor,
        int selectionColor,
        int removeSelectionColor
) {
    public static final Color BLUE = new Color(0, 0, 255);
    public static final Color RED = new Color(255, 0, 0);
    public static final Color GREEN = new Color(0, 255, 0);
    public static final Color SKY_BLUE = new Color(0, 192, 255);
    public static final Color DARK_ORANGE = new Color(255, 127, 0);
    
    public ConfigData (){
        this(
                BLUE.getRGB(),
                RED.getRGB(),
                GREEN.getRGB(),
                SKY_BLUE.getRGB(),
                DARK_ORANGE.getRGB()
        );
    }
    public  ConfigData (
            Color lineColor,
            Color invalidEdgeColor,
            Color validEdgeColor,
            Color selectionColor,
            Color removeSelectionColor
    ) {
        this(
                lineColor.getRGB(),
                invalidEdgeColor.getRGB(),
                validEdgeColor.getRGB(),
                selectionColor.getRGB(),
                removeSelectionColor.getRGB()
        );
    }
}
