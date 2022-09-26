package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.block.entity.ChunkLoadingTesterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChunkLoadingTesterSetSettingsS2CPacket
{
    public final ResourceLocation levelRegistryName;
    public final BlockPos blockPos;
    public final ChunkPos centerChunk;
    public final int radius;
    
    private ChunkLoadingTesterSetSettingsS2CPacket(ResourceLocation levelResourceLocation, BlockPos blockPos, ChunkPos centerChunk, int radius)
    {
        this.levelRegistryName = levelResourceLocation;
        this.blockPos = blockPos;
        this.centerChunk = centerChunk;
        this.radius = radius;
    }
    
    public ChunkLoadingTesterSetSettingsS2CPacket(ChunkLoadingTesterBlockEntity chunkTester)
    {
        this(chunkTester.getLevel().dimension().getRegistryName(), chunkTester.getBlockPos(), chunkTester.getCenterChunk(), chunkTester.getRadius());
    }
    
    public ChunkLoadingTesterSetSettingsS2CPacket(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation(), buf.readBlockPos(), new ChunkPos(buf.readInt(), buf.readInt()), buf.readInt());
    }
    
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(levelRegistryName);
        buf.writeBlockPos(blockPos);
        buf.writeInt(centerChunk.x);
        buf.writeInt(centerChunk.z);
        buf.writeInt(radius);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
         var context = supplier.get();
         context.enqueueWork(() -> {
             
             var loadedLevel = Minecraft.getInstance().level;
             if(loadedLevel.dimension().getRegistryName().equals(levelRegistryName) == false)
                 return;
    
             if(loadedLevel.hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ())) == false)
                return;
             
             if(loadedLevel.getBlockEntity(blockPos) instanceof ChunkLoadingTesterBlockEntity chunkTester)
             {
                 chunkTester.setCenterChunk(centerChunk);
                 if(radius >= 0)
                    chunkTester.setRadius(radius);
             }
         });
         
         context.setPacketHandled(true);
         return true;
    }
}
