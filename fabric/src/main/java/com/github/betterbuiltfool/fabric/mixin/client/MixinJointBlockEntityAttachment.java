package com.github.betterbuiltfool.fabric.mixin.client;

import com.github.betterbuiltfool.blocks.block_entities.JointBlockEntity;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(JointBlockEntity.class)
public class MixinJointBlockEntityAttachment implements RenderDataBlockEntity {
    
    @Override
    public @Nullable Object getRenderData() {
        return ((JointBlockEntity) (Object) this).getRenderData();
    }
}
