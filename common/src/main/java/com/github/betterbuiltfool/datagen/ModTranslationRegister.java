package com.github.betterbuiltfool.datagen;

public class ModTranslationRegister {
    
    public static void register(AutoLangProvider provider) {
        provider.add("tool", "froe", "Froe");
        provider.add("tool", "framing_hammer", "Framing Mallet");
        
        provider.add("tooltip", "framing_hammer", "first_pos", "First Selected Position: %s");
        provider.add("tooltip", "framing_hammer", "selection", "Selection: %s");
        
        provider.add("creative_tab", "tools", "Framing Tools");
        
        provider.add("config", "title", "Dynamic Framing Config");
        
        provider.add("config", "category", "network_view", "Network View");
        provider.add("config", "category", "block_validation", "Block Validation");
        provider.add("config", "category", "structure", "Structure");
        
        provider.add("config", "option", "standard_edge_color", "Normal Line Color");
        provider.add("config", "option", "invalid_edge_color", "Invalid Edge Color");
        provider.add("config", "option", "valid_edge_color", "Valid Edge Color");
        provider.add("config", "option", "selection_color", "Selection Color");
        provider.add("config", "option", "remove_selection_color", "Remove Selection Color");
        
        provider.add("config", "option", "block_replacement_whitelist", "Block Replacement Whitelist");
        provider.add("config", "option", "structure_material_whitelist", "Valid Structure Materials");
        
    }
}
