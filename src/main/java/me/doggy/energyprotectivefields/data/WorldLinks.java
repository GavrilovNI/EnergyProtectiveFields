package me.doggy.energyprotectivefields.data;

import com.google.common.collect.ArrayListMultimap;
import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


public class WorldLinks extends SavedData
{
    public static class LinkInfo
    {
        public final BlockPos blockPos;
        public final UUID uuid;
        
        public LinkInfo(BlockPos blockPos, UUID uuid)
        {
            this.blockPos = blockPos;
            this.uuid = uuid;
        }
    
        public LinkInfo(CompoundTag nbt)
        {
            this.blockPos = NbtUtils.readBlockPos(nbt.getCompound("position"));
            this.uuid = nbt.getUUID("uuid");
        }
        
        public CompoundTag serializeNBT()
        {
            CompoundTag nbt = new CompoundTag();
            nbt.put("position", NbtUtils.writeBlockPos(blockPos));
            nbt.putUUID("uuid", uuid);
            return nbt;
        }
    
        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            LinkInfo that = (LinkInfo)o;
            return Objects.equals(blockPos, that.blockPos) && Objects.equals(uuid, that.uuid);
        }
    
        @Override
        public int hashCode()
        {
            return Objects.hash(blockPos, uuid);
        }
    }
    
    private final ArrayListMultimap<LinkInfo, BlockPos> controllerToOthers = ArrayListMultimap.create();
    private final ServerLevel level;
    
    public static WorldLinks get(ServerLevel level)
    {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent((tag) -> new WorldLinks(level, tag), () -> new WorldLinks(level), EnergyProtectiveFields.MOD_ID + "_links");
    }
    
    public static LinkInfo getControllerLinkInfo(FieldControllerBlockEntity controller)
    {
        return new LinkInfo(controller.getBlockPos(), controller.getUUID());
    }
    
    public WorldLinks(ServerLevel level)
    {
        this.level = level;
    }
    
    public WorldLinks(ServerLevel level, CompoundTag nbt)
    {
        this(level);
        
        ListTag blockLinks = nbt.getList("controller_block_links", Tag.TAG_COMPOUND);
        for(Tag controllerTag : blockLinks)
        {
            CompoundTag controllerCompoundTag = (CompoundTag)controllerTag;
            var controllerInfo = new LinkInfo(controllerCompoundTag.getCompound("controller_link_info"));
            
            ListTag othersList = controllerCompoundTag.getList("links", Tag.TAG_COMPOUND);
            
            for(Tag posTag : othersList)
            {
                CompoundTag posCompoundTag = (CompoundTag)posTag;
                controllerToOthers.put(controllerInfo, NbtUtils.readBlockPos(posCompoundTag));
            }
        }
    }
    
    @Override
    public CompoundTag save(CompoundTag nbt)
    {
        ListTag blockLinks = new ListTag();
        for(var entry : controllerToOthers.asMap().entrySet())
        {
            CompoundTag controllerTag = new CompoundTag();
            var controllerInfo = entry.getKey();
            controllerTag.put("controller_link_info", controllerInfo.serializeNBT());
            
            ListTag othersList = new ListTag();
            for(var otherPos : entry.getValue())
            {
                othersList.add(NbtUtils.writeBlockPos(otherPos));
            }
            controllerTag.put("links", othersList);
            
            blockLinks.add(controllerTag);
        }
        nbt.put("controller_block_links", blockLinks);
        return nbt;
    }
    
    @Nullable
    private FieldControllerBlockEntity getController(LinkInfo controllerInfo)
    {
        if(level.getBlockEntity(controllerInfo.blockPos) instanceof FieldControllerBlockEntity controller
                && controller.getUUID().equals(controllerInfo.uuid))
        {
            return controller;
        }
        return null;
    }
    
    public void removeLinksByController(FieldControllerBlockEntity controller)
    {
        var controllerPosition = controller.getBlockPos();
        var linkedBlockPoses = controllerToOthers.removeAll(new LinkInfo(controllerPosition, controller.getUUID()));
        
        for(var position : linkedBlockPoses)
        {
            controller.onUnlinked(level, position);
            
            setDirty();
        }
    }
    
    public void addLink(LinkInfo controllerInfo, BlockPos blockPos)
    {
        var controller = getController(controllerInfo);
        
        if(controller != null && controllerToOthers.containsEntry(controllerInfo, blockPos) == false)
        {
            controllerToOthers.put(controllerInfo, blockPos);
            controller.onLinked(level, blockPos);
            
            setDirty();
        }
    }
    
    public void removeLink(LinkInfo controllerInfo, BlockPos blockPos)
    {
        if(controllerToOthers.remove(controllerInfo, blockPos))
        {
            var controller = getController(controllerInfo);
            if(controller != null)
                controller.onUnlinked(level, blockPos);
            
            setDirty();
        }
    }
    
    public List<BlockPos> getLinks(LinkInfo controllerInfo)
    {
        return List.copyOf(controllerToOthers.get(controllerInfo));
    }
    
    public void removeLinkForAllControllers(BlockPos blockPos)
    {
        for(var key : controllerToOthers.keySet().stream().toList())
        {
            removeLink(key, blockPos);
        }
        setDirty();
    }
    
    public boolean hasLink(LinkInfo controllerInfo, BlockPos blockPos)
    {
        return controllerToOthers.containsEntry(controllerInfo, blockPos);
    }
}
