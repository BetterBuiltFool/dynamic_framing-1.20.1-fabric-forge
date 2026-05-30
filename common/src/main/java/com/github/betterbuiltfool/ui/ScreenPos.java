package com.github.betterbuiltfool.ui;

public record ScreenPos(int x, int y) {
    
    public String toShortString() {
        return String.format("[%s, %s]", x, y);
    }
}
