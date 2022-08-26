package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.block.entity.InfinityEnergyGeneratorBlockEntity;
import me.doggy.energyprotectivefields.api.utils.AntiCheat;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TestEnergyGeneratorSetMaxExtractC2SPacket
{
    private final BlockPos blockPos;
    private final int maxExtract;
    
    public TestEnergyGeneratorSetMaxExtractC2SPacket(int maxExtract, BlockPos blockPos)
    {
        this.maxExtract = maxExtract;
        this.blockPos = blockPos;
    }
    
    public TestEnergyGeneratorSetMaxExtractC2SPacket(FriendlyByteBuf buf)
    {
        this(buf.readInt(), buf.readBlockPos());
    }
    
    public void toBytes(FriendlyByteBuf buf)
    {
        buf.writeInt(maxExtract);
        buf.writeBlockPos(blockPos);
    }
    
    public boolean handle(Supplier<NetworkEvent.Context> supplier)
    {
         var context = supplier.get();
         context.enqueueWork(() -> {
             ServerPlayer player = context.getSender();
             var level = player.getLevel();
    
             if(AntiCheat.canReachAndInteract(player, level, blockPos) == false)
                 return;
             if(level.getBlockEntity(blockPos) instanceof InfinityEnergyGeneratorBlockEntity blockEntity)
             {
                 try
                 {
                     blockEntity.setMaxEnergyExtract(maxExtract);
                 }
                 catch(IllegalArgumentException exception)
                 {
            
                 }
             }
         });
         context.setPacketHandled(true);
         return true;
    }
}
