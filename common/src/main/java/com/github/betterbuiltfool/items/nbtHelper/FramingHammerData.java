package com.github.betterbuiltfool.items.nbtHelper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FramingHammerData {
    public static final String FIRST_POS_DATA = "FirstPosData";
    public static final String SECOND_POS_DATA = "SecondPosData";
    
    public final ItemStack wrapped;
    private Long firstPos;
    private Long secondPos;
    
    public FramingHammerData(@NotNull ItemStack hammerTool) {
        this.wrapped = hammerTool;
        this.firstPos = getPos(FIRST_POS_DATA);
        this.secondPos = getPos(SECOND_POS_DATA);
    }
    
    //region Public Fields
    public long getFirstPos() {
        return firstPos;
    }
    
    public long getSecondPos() {
        return secondPos;
    }
    
    public boolean hasFirstPos() {
        return firstPos != null;
    }
    
    public boolean hasSecondPos() {
        return secondPos != null;
    }
    
    public void setFirstPos(long firstPos) {
        this.firstPos = firstPos;
        setPos(FIRST_POS_DATA, firstPos);
    }
    
    public void setSecondPos(long secondPos) {
        this.secondPos = secondPos;
        setPos(SECOND_POS_DATA, secondPos);
    }
    
    public void clearFirstPos() {
        clearPos(FIRST_POS_DATA);
    }
    
    public void clearSecondPos() {
        clearPos(SECOND_POS_DATA);
    }
    
    public void clear() {
        clearFirstPos();
        clearSecondPos();
    }
    //endregion
    
    //region Private NBT Modifiers
    @Nullable
    private Long getPos(
            String nbtKey
    ) {
        CompoundTag posTag = this.wrapped.getTagElement(nbtKey);
        
        if (posTag == null) {
            return null;
        }
        
        return posTag.getLong("PosData");
        
    }
    
    private void clearPos(
            String nbtKey
    ) {
        this.wrapped.removeTagKey(nbtKey);
    }
    
    private void setPos(
            String nbtKey,
            long pos
    ) {
        CompoundTag posTag = this.wrapped.getOrCreateTag();
        
        CompoundTag posData = new CompoundTag();
        posData.putLong("PosData", pos);
        
        posTag.put(nbtKey, posData);
        
    }
    //endregion
}
