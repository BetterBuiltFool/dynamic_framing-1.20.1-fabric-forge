package com.github.betterbuiltfool.datagen;

public enum Category {
    ITEM("item"),
    TOOLTIP("tooltip"),
    CREATIVE_TAB("creative_tab"),
    CONFIG("config");
    
    private final String key;
    
    Category(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return key;
    }
}
