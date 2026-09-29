package com.github.betterbuiltfool.fabric.mixin.client;

import com.github.betterbuiltfool.blocks.block_entities.StructureMemberBlockEntity;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureMemberBlockEntity.class)
public class MixinBeamBlockEntityAttachment implements RenderDataBlockEntity {
    
    @Override
    public @Nullable Object getRenderData() {
        return ((StructureMemberBlockEntity) (Object) this).getRenderData();
    }
}
