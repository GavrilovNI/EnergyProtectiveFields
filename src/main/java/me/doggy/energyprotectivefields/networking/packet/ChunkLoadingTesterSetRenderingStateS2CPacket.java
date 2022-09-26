package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.block.entity.ChunkLoadingTesterBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ChunkLoadingTesterSetRenderingStateS2CPacket
{
    private final ResourceLocation levelRegistryName;
    private final BlockPos blockPos;
    private final BlockState blockState;
    
    private ChunkLoadingTesterSetRenderingStateS2CPacket(ResourceLocation levelResourceLocation, BlockPos blockPos, BlockState blockState)
    {
        this.blockPos = blockPos;
        this.levelRegistryName = levelResourceLocation;
        this.blockState = blockState;
    }
    
    public ChunkLoadingTesterSetRenderingStateS2CPacket(ChunkLoadingTesterBlockEntity chunkTester)
    {
        this(chunkTester.getLevel().dimension().getRegistryName(), chunkTester.getBlockPos(), chunkTester.getBlockStateToRender());
    }
    
    public ChunkLoadingTesterSetRenderingStateS2CPacket(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation(), buf.readBlockPos(), ForgeRegistries.BLOCKS.getValue(buf.readResourceLocation()).defaultBlockState());
    }
    
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(levelRegistryName);
        buf.writeBlockPos(blockPos);
        buf.writeResourceLocation(blockState.getBlock().getRegistryName());
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
                 chunkTester.setBlockStateToRender(blockState);
         });
         
         context.setPacketHandled(true);
         return true;
    }
}
