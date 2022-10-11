package me.doggy.energyprotectivefields.data;


import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.event.LevelChunkEvent;
import net.minecraft.core.BlockPos;
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
            get(serverLevel).levelChunkBlockStateChanged(event.levelChunk.getPos(), event.blockPos, event.oldState, event.newState, level.getGameTime());
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
    
    public void levelChunkBlockStateChanged(ChunkPos chunkPos, BlockPos blockPos, BlockState oldState, BlockState newState, long gameTime)
    {
        boolean shouldCaptureForChunkChanges = oldState.isAir() == false && newState.isAir() && WorldFieldsBounds.get(level).hasControllersByChunk(chunkPos);
        boolean shouldCaptureForProjectors = shouldCaptureForChunkChanges && (oldState.getBlock() instanceof FieldBlock == false);
        
        if(shouldCaptureForChunkChanges)
            lastTimeChunksUpdated.put(chunkPos, gameTime);
        
        if(shouldCaptureForProjectors)
        {
            var controllerPositions = WorldFieldsBounds.get(level).getControllersByChunk(chunkPos);
            
            for(var controllerPosition : controllerPositions)
            {
                if(level.getBlockEntity(controllerPosition) instanceof IFieldProjector fieldProjector)
                    fieldProjector.queueFieldForCreatingIfInShape(blockPos);
            }
        }
    }
    
    @Nullable
    public Long getLastTimeChunkUpdated(ChunkPos chunkPos)
    {
        return lastTimeChunksUpdated.get(chunkPos);
    }
    
    public boolean isChunkUpdatedAfter(ChunkPos chunkPos, long gameTime)
    {
        var lastTimeUpdated = lastTimeChunksUpdated.get(chunkPos);
        if(lastTimeUpdated == null)
        {
            return false;
        }
        else
        {
            return lastTimeUpdated > gameTime;
        }
    }
    
    public boolean isChunkUpdatedNotBefore(ChunkPos chunkPos, long gameTime)
    {
        var lastTimeUpdated = lastTimeChunksUpdated.get(chunkPos);
        if(lastTimeUpdated == null)
        {
            return false;
        }
        else
        {
            return lastTimeUpdated >= gameTime;
        }
    }
    
    public void onRemovedAllControllersFromChunk(ChunkPos chunkPos)
    {
        lastTimeChunksUpdated.remove(chunkPos);
    }
}
