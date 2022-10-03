package me.doggy.energyprotectivefields.data;

import com.google.common.collect.HashMultimap;
import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.utils.MoreNbtUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.Objects;

public class WorldChunksLoader extends SavedData
{
    public static class ForcingInfo implements INBTSerializable<CompoundTag>
    {
        private ChunkPos chunkPos;
        private BlockPos blockLoader;
        
        public ForcingInfo(ChunkPos chunkPos, BlockPos blockLoader)
        {
            this.chunkPos = chunkPos;
            this.blockLoader = blockLoader;
        }
        
        public ChunkPos getChunkPos()
        {
            return chunkPos;
        }
        
        public BlockPos getBlockLoader()
        {
            return blockLoader;
        }
    
        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            ForcingInfo that = (ForcingInfo)o;
            return Objects.equals(chunkPos, that.chunkPos) && Objects.equals(blockLoader, that.blockLoader);
        }
    
        @Override
        public int hashCode()
        {
            return Objects.hash(chunkPos, blockLoader);
        }
    
        @Override
        public CompoundTag serializeNBT()
        {
            CompoundTag result = new CompoundTag();
            result.put("chunk_pos", MoreNbtUtils.writeChunkPos(chunkPos));
            result.put("block_loader", NbtUtils.writeBlockPos(blockLoader));
            return result;
        }
    
        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            chunkPos = MoreNbtUtils.readChunkPos(nbt.getCompound("chunk_pos"));
            blockLoader = NbtUtils.readBlockPos(nbt.getCompound("block_loader"));
        }
    }
    
    private final ServerLevel level;
    private final HashMultimap<ForcingInfo, BlockPos> initiatorByInfo = HashMultimap.create();
    
    public static WorldChunksLoader get(ServerLevel level)
    {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> new WorldChunksLoader(level, tag), () -> new WorldChunksLoader(level), EnergyProtectiveFields.MOD_ID + "_chunks_loader");
    }
    
    public WorldChunksLoader(ServerLevel level)
    {
        this.level = level;
    }
    
    public WorldChunksLoader(ServerLevel level, CompoundTag nbt)
    {
        this(level);
        
        ListTag infosTag = nbt.getList("infos", Tag.TAG_COMPOUND);
        
        for(int i = 0; i < infosTag.size(); ++i)
        {
            CompoundTag infoTag = infosTag.getCompound(i);
            
            ForcingInfo forcingInfo = new ForcingInfo(null, null);
            forcingInfo.deserializeNBT(infoTag.getCompound("forcing_info"));
    
            ListTag initiatorsTag = infoTag.getList("initiators", Tag.TAG_COMPOUND);
    
            for(int j = 0; j < initiatorsTag.size(); ++j)
            {
                var initiator = NbtUtils.readBlockPos(initiatorsTag.getCompound(j));
                initiatorByInfo.put(forcingInfo, initiator);
            }
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag pCompoundTag)
    {
        ListTag infosTag = new ListTag();
        for(var forcingInfo : initiatorByInfo.keySet())
        {
            ListTag initiatorsTag = new ListTag();
            for(var initiator : initiatorByInfo.get(forcingInfo))
                initiatorsTag.add(NbtUtils.writeBlockPos(initiator));
    
            CompoundTag infoTag = new CompoundTag();
            infoTag.put("forcing_info", forcingInfo.serializeNBT());
            infoTag.put("initiators", initiatorsTag);
            
            infosTag.add(infoTag);
        }
    
        pCompoundTag.put("infos", infosTag);
        
        return pCompoundTag;
    }
    
    
    public void forceChunk(ChunkPos chunkPos, BlockPos blockLoader, BlockPos initiator)
    {
        if(new ChunkPos(blockLoader).equals(chunkPos))
            return;
        
        var forcingInfo = new ForcingInfo(chunkPos, blockLoader);
        if(initiatorByInfo.containsEntry(forcingInfo, initiator))
            return;
    
        int alreadyForcingCount = initiatorByInfo.get(forcingInfo).size();
        if(alreadyForcingCount == 0)
        {
            boolean forced = ForgeChunkManager.forceChunk(level, EnergyProtectiveFields.MOD_ID, blockLoader, chunkPos.x, chunkPos.z, true, true);
            /*if(forced)
            {
                EnergyProtectiveFields.LOGGER.debug("Forced chunk" + chunkPos +
                        " with loader at (" + blockLoader.toShortString() +
                        ")" + new ChunkPos(blockLoader) +
                        " and initiator at (" + initiator.toShortString() +
                        ")" + new ChunkPos(initiator));
            }
            else
            {
                EnergyProtectiveFields.LOGGER.info("Couldn't force chunk " + chunkPos +
                        " with loader at (" + blockLoader.toShortString() +
                        ")" + new ChunkPos(blockLoader) +
                        " and initiator at (" + initiator.toShortString() +
                        ")" + new ChunkPos(initiator));
            }*/
        }
    
        initiatorByInfo.put(forcingInfo, initiator);
        setDirty();
    }
    
    public void stopForcingChunk(ChunkPos chunkPos, BlockPos blockLoader, BlockPos initiator)
    {
        var forcingInfo = new ForcingInfo(chunkPos, blockLoader);
        if(initiatorByInfo.containsEntry(forcingInfo, initiator) == false)
            return;
    
        int leftCount = initiatorByInfo.get(forcingInfo).size();
        if(leftCount == 1)
        {
            boolean stopped = ForgeChunkManager.forceChunk(level, EnergyProtectiveFields.MOD_ID, blockLoader, chunkPos.x, chunkPos.z, false, true);
            /*if(stopped)
            {
                EnergyProtectiveFields.LOGGER.debug("Stopped forcing chunk " + chunkPos +
                        " with loader at (" + blockLoader.toShortString() +
                        ")" + new ChunkPos(blockLoader) +
                        " and initiator at (" + initiator.toShortString() +
                        ")" + new ChunkPos(initiator));
            }
            else
            {
                EnergyProtectiveFields.LOGGER.info("Couldn't stop forcing chunk " + chunkPos +
                        " with loader at (" + blockLoader.toShortString() +
                        ")" + new ChunkPos(blockLoader) +
                        " and initiator at (" + initiator.toShortString() +
                        ")" + new ChunkPos(initiator));
            }*/
        }
        
        initiatorByInfo.remove(forcingInfo, initiator);
        setDirty();
    }
}
