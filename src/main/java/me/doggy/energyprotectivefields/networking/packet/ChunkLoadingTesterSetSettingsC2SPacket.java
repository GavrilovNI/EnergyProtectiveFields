package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.screen.ChunkLoadingTesterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChunkLoadingTesterSetSettingsC2SPacket
{
    private final int containerId;
    private final ChunkPos centerChunk;
    private final int radius;
    
    public ChunkLoadingTesterSetSettingsC2SPacket(int containerId, ChunkPos centerChunk, int radius)
    {
        this.containerId = containerId;
        this.centerChunk = centerChunk;
        this.radius = radius;
    }
    
    public ChunkLoadingTesterSetSettingsC2SPacket(FriendlyByteBuf buf)
    {
        this(buf.readInt(), new ChunkPos(buf.readInt(), buf.readInt()), buf.readInt());
    }
    
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeInt(containerId);
        buf.writeInt(centerChunk.x);
        buf.writeInt(centerChunk.z);
        buf.writeInt(radius);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
         var context = supplier.get();
         context.enqueueWork(() -> {
             ServerPlayer player = context.getSender();
             
             if(player.containerMenu.containerId != containerId)
                 return;
             
             if(player.containerMenu instanceof ChunkLoadingTesterMenu menu)
             {
                 menu.setCenterChunk(centerChunk);
                 if(radius >= 0)
                     menu.setRadius(radius);
             }
         });
         context.setPacketHandled(true);
         return true;
    }
}
