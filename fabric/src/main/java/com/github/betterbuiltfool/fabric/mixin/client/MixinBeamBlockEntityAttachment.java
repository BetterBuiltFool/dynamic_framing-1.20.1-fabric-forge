package com.github.betterbuiltfool.fabric.mixin.client;

import com.github.betterbuiltfool.blocks.block_entities.BeamBlockEntity;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BeamBlockEntity.class)
public class MixinBeamBlockEntityAttachment implements RenderDataBlockEntity {
    
    @Override
    public @Nullable Object getRenderData() {
        return ((BeamBlockEntity) (Object) this).getRenderData();
    }
}
