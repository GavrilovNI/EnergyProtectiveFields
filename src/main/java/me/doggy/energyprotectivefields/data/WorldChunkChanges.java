package me.doggy.energyprotectivefields.data;


import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.event.LevelChunkEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class WorldChunkChanges extends SavedData
{
    @SubscribeEvent
    public static void onLevelChunkSectionBlockStateChanged(LevelChunkEvent.BlockStateChanged event)
    {
        var level = event.levelChunk.getLevel();
        if(level instanceof ServerLevel serverLevel)
            get(serverLevel).levelChunkBlockStateChanged(event.levelChunk.getPos(), event.oldState, event.newState, level.getGameTime());
    }
    
    public static WorldChunkChanges get(ServerLevel level)
    {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> new WorldChunkChanges(level), () -> new WorldChunkChanges(level), EnergyProtectiveFields.MOD_ID + "_world_chunk_changes");
    }
    
    public final ServerLevel level;
    private final HashMap<ChunkPos, Long> lastTimeChunksUpdated = new HashMap<>();
    
    public WorldChunkChanges(ServerLevel level)
    {
        this.level = level;
    }
    
    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        return nbt;
    }
    
    public void levelChunkBlockStateChanged(ChunkPos chunkPos, BlockState oldState, BlockState newState, long gameTime)
    {
        boolean shouldCapture = oldState.isAir() == false && newState.isAir() && WorldFieldsBounds.get(level).hasControllersByChunk(chunkPos);
        
        if(shouldCapture)
            lastTimeChunksUpdated.put(chunkPos, gameTime);
    }
    
    @Nullable
    public Long getLastTimeChunkUpdated(ChunkPos chunkPos)
    {
        return lastTimeChunksUpdated.get(chunkPos);
    }
    
    public boolean isChunkUpdatedAfter(ChunkPos chunkPos, long instant)
    {
        var lastTimeUpdated = lastTimeChunksUpdated.get(chunkPos);
        if(lastTimeUpdated == null)
        {
            return false;
        }
        else
        {
            return lastTimeUpdated > instant;
        }
    }
    
    public void onRemovedAllControllersFromChunk(ChunkPos chunkPos)
    {
        lastTimeChunksUpdated.remove(chunkPos);
    }
}
