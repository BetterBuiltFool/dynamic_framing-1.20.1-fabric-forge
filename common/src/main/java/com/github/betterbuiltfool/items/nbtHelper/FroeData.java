package com.github.betterbuiltfool.items.nbtHelper;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.data.GraphHitNbtData;
import com.github.betterbuiltfool.structure.GraphHit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FroeData {
    public static final String LOOK_POS_DATA;
    public static final String CONTAINER_KEY;
    
    public final ItemStack wrapped;
    @NotNull
    private final CompoundTag containerTag;
    private Long lookPos;
    private GraphHit.EdgeHit selection;
    
    static {
        LOOK_POS_DATA = DynamicFraming.MOD_ID + ":look_pos";
        CONTAINER_KEY = DynamicFraming.MOD_ID + ":framing_hammer_data";
    }
    
    public FroeData(ItemStack froeTool) {
        this.wrapped = froeTool;
        
        var rootNbt = froeTool.getOrCreateTag();
        CompoundTag container;
        if (rootNbt.contains(CONTAINER_KEY)) {
            container = rootNbt.getCompound(CONTAINER_KEY);
        } else {
            container = new CompoundTag();
        }
        this.containerTag = container;
        rootNbt.put(CONTAINER_KEY, this.containerTag);
        
        this.lookPos = getPos(LOOK_POS_DATA);
        this.selection = (GraphHit.EdgeHit) GraphHitNbtData.loadGraphHit(this.containerTag);
    }
    
    public boolean hasLookPos() {
        return lookPos != null;
    }
    
    public long getLookPos() {
        return lookPos;
    }
    
    public void setLookPos(long lookPos) {
        this.lookPos = lookPos;
        setPos(LOOK_POS_DATA, lookPos);
    }
    
    public boolean hasSelection() {
        return selection != null;
    }
    
    public GraphHit.EdgeHit getSelection() {
        return selection;
    }
    
    public void setSelection(GraphHit.EdgeHit newSelection) {
        this.selection = newSelection;
        GraphHitNbtData.saveGraphHit(this.containerTag, this.selection);
    }
    
    //region Private NBT Modifiers
    @Nullable
    private Long getPos(
            String nbtKey
    ) {
        if (!this.containerTag.contains(nbtKey)) {
            return null;
        }
        
        return this.containerTag.getLong(nbtKey);
        
    }
    
    private void clearPos(
            String nbtKey
    ) {
        this.containerTag.remove(nbtKey);
    }
    
    private void setPos(
            String nbtKey,
            long pos
    ) {
        this.containerTag.putLong(nbtKey, pos);
        
    }
    //endregion
}
