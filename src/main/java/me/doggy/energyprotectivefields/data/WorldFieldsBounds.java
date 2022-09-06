package me.doggy.energyprotectivefields.data;

import com.google.common.collect.HashMultimap;
import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.utils.MoreNbtUtils;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldFieldsBounds extends SavedData
{
    private final ServerLevel level;
    private final HashMultimap<ChunkPos, BlockPos> chunkToController = HashMultimap.create();
    
    
    public static WorldFieldsBounds get(ServerLevel level)
    {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> new WorldFieldsBounds(level, tag), () -> new WorldFieldsBounds(level), EnergyProtectiveFields.MOD_ID + "_fields_bounds");
    }
    
    public WorldFieldsBounds(ServerLevel level)
    {
        this.level = level;
    }
    
    public WorldFieldsBounds(ServerLevel level, CompoundTag nbt)
    {
        this(level);
    
    
        ListTag mainList = nbt.getList("fields_bounds", Tag.TAG_COMPOUND);
        
        for(var tag : mainList)
        {
            CompoundTag chunkTag = (CompoundTag)tag;
            
            ChunkPos chunkPos = MoreNbtUtils.readChunkPos(chunkTag.getCompound("chunk_pos"));
            ListTag controllers = chunkTag.getList("controllers", Tag.TAG_COMPOUND);
            
            for(var controllerTag : controllers)
            {
                BlockPos controllerPos = NbtUtils.readBlockPos((CompoundTag)controllerTag);
                chunkToController.put(chunkPos, controllerPos);
            }
        }
    }
    
    public Set<BlockPos> getControllersByChunk(ChunkPos chunkPos)
    {
        return chunkToController.get(chunkPos).stream().collect(Collectors.toSet());
    }
    
    public void removeController(BlockPos blockPos)
    {
        setDirty();
        chunkToController.values().remove(blockPos);
    }
    
    private static HashSet<ChunkPos> getChunksInBounds(Level level, BoundingBox boundingBox)
    {
        HashSet<ChunkPos> chunks = new HashSet<>();
    
        ChunkPos minChunk = level.getChunk(new BlockPos(boundingBox.minX(), 0, boundingBox.minZ())).getPos();
        ChunkPos maxChunk = level.getChunk(new BlockPos(boundingBox.maxX(), 0, boundingBox.maxZ())).getPos();
        
        for(int x = minChunk.x; x <= maxChunk.x; ++x)
            for(int z = minChunk.z; z <= maxChunk.z; ++z)
                chunks.add(new ChunkPos(x, z));
            
        return chunks;
    }
    
    public void updateController(FieldControllerBlockEntity controller)
    {
        setDirty();
        
        removeController(controller.getBlockPos());
    
        BoundingBox boundingBox = controller.getShapeBounds();
        if(boundingBox == null)
            return;
        
        var chunks = getChunksInBounds(controller.getLevel(), boundingBox);
        var controllerPos = controller.getBlockPos();
        
        for(var chunk : chunks)
            chunkToController.put(chunk, controllerPos);
    }
    
    @Override
    public CompoundTag save(CompoundTag pCompoundTag)
    {
        ListTag mainList = new ListTag();
        pCompoundTag.put("fields_bounds", mainList);
        
        for(var entry : chunkToController.asMap().entrySet())
        {
            var chunkPos = entry.getKey();
            var controllersPoses = entry.getValue();
            if(controllersPoses.isEmpty())
                continue;
            
            CompoundTag chunkTag = new CompoundTag();
            ListTag controllers = new ListTag();
    
            mainList.add(chunkTag);
            
            chunkTag.put("chunk_pos", MoreNbtUtils.writeChunkPos(chunkPos));
            chunkTag.put("controllers", controllers);
            
            for(var controllerPos : controllersPoses)
                controllers.add(NbtUtils.writeBlockPos(controllerPos));
        }
        
        return pCompoundTag;
    }
}
