package com.github.betterbuiltfool.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public abstract class Command {
    
    public abstract String getName();
    
    public int getPermissionLevel() {
        return 2;
    }
    
    public abstract int execute(CommandContext<CommandSourceStack> context);
    
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
    
    }
    
    
    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(getName())
                                                                     .requires(source -> source.hasPermission(getPermissionLevel()))
                                                                     .executes(this::execute);
        build(builder);
        dispatcher.register(builder);
    }
}
