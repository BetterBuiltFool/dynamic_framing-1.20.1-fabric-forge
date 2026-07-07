package com.github.betterbuiltfool.commands;

import com.github.betterbuiltfool.data.FramedStructureStorage;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class ClearAllCommand extends Command {
    
    public String getName() {
        return "clearall";
    }
    
    public int execute(CommandContext<CommandSourceStack> context) {
        var source = context.getSource();
        var level = source.getLevel();
        
        FramedStructureStorage.clearAll(level);
        if (source.getPlayer() != null) {
            source.sendSuccess(() -> Component.literal("Cleared all structure nodes"), true);
        }
        
        return 1;
    }
}
