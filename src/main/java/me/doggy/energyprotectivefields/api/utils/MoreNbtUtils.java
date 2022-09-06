package me.doggy.energyprotectivefields.api.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

public class MoreNbtUtils
{
    public static CompoundTag writeChunkPos(ChunkPos chunkPos)
    {
        CompoundTag compoundtag = new CompoundTag();
        compoundtag.putInt("X", chunkPos.x);
        compoundtag.putInt("Z", chunkPos.z);
        return compoundtag;
    }
    
    public static ChunkPos readChunkPos(CompoundTag nbt)
    {
        return new ChunkPos(nbt.getInt("X"), nbt.getInt("Z"));
    }
}
