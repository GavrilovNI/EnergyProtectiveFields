package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.screen.InfinityEnergyGeneratorMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class InfinityEnergyGeneratorSetMaxExtractC2SPacket
{
    private final int containerId;
    private final int maxExtract;
    
    public InfinityEnergyGeneratorSetMaxExtractC2SPacket(int containerId, int maxExtract)
    {
        this.maxExtract = maxExtract;
        this.containerId = containerId;
    }
    
    public InfinityEnergyGeneratorSetMaxExtractC2SPacket(FriendlyByteBuf buf)
    {
        this(buf.readInt(), buf.readInt());
    }
    
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(containerId);
        buf.writeInt(maxExtract);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
         var context = supplier.get();
         context.enqueueWork(() -> {
             ServerPlayer player = context.getSender();
             
             if(player.containerMenu.containerId != containerId)
                 return;
             
             if(player.containerMenu instanceof InfinityEnergyGeneratorMenu menu)
                 menu.setMaxEnergyExtract(maxExtract);
         });
         context.setPacketHandled(true);
         return true;
    }
}
