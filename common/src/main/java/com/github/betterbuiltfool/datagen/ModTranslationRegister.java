package com.github.betterbuiltfool.datagen;

public class ModTranslationRegister {
    
    public static void register(AutoLangProvider provider) {
        provider.add(Category.ITEM, "froe", "Froe");
        provider.add(Category.ITEM, "framing_hammer", "Framing Mallet");
        
        provider.add(Category.TOOLTIP, "framing_hammer", "first_pos", "First Selected Position: %s");
        provider.add(Category.TOOLTIP, "framing_hammer", "selection", "Selection: %s");
        
        provider.add(Category.CREATIVE_TAB, "tools", "Framing Tools");
        
        provider.add(Category.CONFIG, "title", "Dynamic Framing Config");
        
        provider.add(Category.CONFIG, "category", "network_view", "Network View");
        provider.add(Category.CONFIG, "category", "block_validation", "Block Validation");
        provider.add(Category.CONFIG, "category", "structure", "Structure");
        
        provider.add(Category.CONFIG, "option", "standard_edge_color", "Normal Line Color");
        provider.add(Category.CONFIG, "option", "invalid_edge_color", "Invalid Edge Color");
        provider.add(Category.CONFIG, "option", "valid_edge_color", "Valid Edge Color");
        provider.add(Category.CONFIG, "option", "selection_color", "Selection Color");
        provider.add(Category.CONFIG, "option", "remove_selection_color", "Remove Selection Color");
        
        provider.add(Category.CONFIG, "option", "block_replacement_whitelist", "Block Replacement Whitelist");
        provider.add(Category.CONFIG, "option", "structure_material_whitelist", "Valid Structure Materials");
        
    }
}
