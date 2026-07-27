package com.github.betterbuiltfool.items.nbtHelper;

import com.github.betterbuiltfool.DynamicFraming;
import com.github.betterbuiltfool.data.GraphHitNbtData;
import com.github.betterbuiltfool.structure.GraphHit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FramingHammerData {
    public static final String FIRST_POS_DATA;
    public static final String SECOND_POS_DATA;
    public static final String CONTAINER_KEY;
    
    public final ItemStack wrapped;
    @NotNull
    private final CompoundTag containerTag;
    private Long firstPos;
    private Long secondPos;
    private GraphHit selection;
    
    static {
        FIRST_POS_DATA = DynamicFraming.MOD_ID + ":first_pos";
        SECOND_POS_DATA = DynamicFraming.MOD_ID + ":second_pos";
        CONTAINER_KEY = DynamicFraming.MOD_ID + ":framing_hammer_data";
    }
    
    public FramingHammerData(@NotNull ItemStack hammerTool) {
        this.wrapped = hammerTool;
        
        var rootNbt = hammerTool.getOrCreateTag();
        CompoundTag container;
        if (rootNbt.contains(CONTAINER_KEY)) {
            container = rootNbt.getCompound(CONTAINER_KEY);
        } else {
            container = new CompoundTag();
        }
        this.containerTag = container;
        rootNbt.put(CONTAINER_KEY, this.containerTag);
        
        this.firstPos = getPos(FIRST_POS_DATA);
        this.secondPos = getPos(SECOND_POS_DATA);
        this.selection = GraphHitNbtData.loadGraphHit(this.containerTag);
    }
    
    //region Public Fields
    public long getFirstPos() {
        return firstPos;
    }
    
    public long getSecondPos() {
        return secondPos;
    }
    
    public GraphHit getSelection() {
        return selection;
    }
    
    public boolean hasFirstPos() {
        return firstPos != null;
    }
    
    public boolean hasSecondPos() {
        return secondPos != null;
    }
    
    public boolean hasSelection() {
        return selection != null;
    }
    
    public void setFirstPos(long firstPos) {
        this.firstPos = firstPos;
        setPos(FIRST_POS_DATA, firstPos);
    }
    
    public void setSecondPos(long secondPos) {
        this.secondPos = secondPos;
        setPos(SECOND_POS_DATA, secondPos);
    }
    
    public void setSelection(GraphHit selection) {
        this.selection = selection;
        GraphHitNbtData.saveGraphHit(this.containerTag, selection);
    }
    
    public void clearFirstPos() {
        clearPos(FIRST_POS_DATA);
    }
    
    public void clearSecondPos() {
        clearPos(SECOND_POS_DATA);
    }
    
    public void clearSelection() {
        GraphHitNbtData.saveGraphHit(this.containerTag, null);
    }
    
    public void clear() {
        clearFirstPos();
        clearSecondPos();
        clearSelection();
    }
    //endregion
    
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
