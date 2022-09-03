package me.doggy.energyprotectivefields.networking.packet;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.INetSerializable;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorageWithStats;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateEnergyCapabilityInBlockEntityS2CPacket
{
    private final BlockPos blockPos;
    private final ResourceLocation levelRegistryName;
    private final BetterEnergyStorage energyStorage;
    private final boolean withStats;
    
    private UpdateEnergyCapabilityInBlockEntityS2CPacket(ResourceLocation levelResourceLocation, BlockPos blockPos, BetterEnergyStorage energyStorage)
    {
        this.blockPos = blockPos;
        this.levelRegistryName = levelResourceLocation;
        this.energyStorage = energyStorage;
        this.withStats = energyStorage instanceof BetterEnergyStorageWithStats;
    }
    
    private UpdateEnergyCapabilityInBlockEntityS2CPacket(ResourceLocation levelRegistryName, BlockPos blockPos, boolean withStats)
    {
        this(levelRegistryName, blockPos, withStats ? new BetterEnergyStorageWithStats() : new BetterEnergyStorage());
    }
    
    public UpdateEnergyCapabilityInBlockEntityS2CPacket(Level level, BlockPos blockPos, BetterEnergyStorage energyStorage)
    {
        this(level.dimension().getRegistryName(), blockPos, energyStorage);
    }
    
    public UpdateEnergyCapabilityInBlockEntityS2CPacket(FriendlyByteBuf buf)
    {
        this(buf.readResourceLocation(), buf.readBlockPos(), buf.readBoolean());
        energyStorage.deserializeNet(buf);
    }
    
    public void serialize(FriendlyByteBuf buf)
    {
        buf.writeResourceLocation(levelRegistryName);
        buf.writeBlockPos(blockPos);
        buf.writeBoolean(withStats);
        energyStorage.serializeNet(buf);
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
             
             var blockEntity = loadedLevel.getBlockEntity(blockPos);
             if(blockEntity != null)
             {
                 var energyStorage = blockEntity.getCapability(CapabilityEnergy.ENERGY).orElse(null);
                 if(energyStorage != null && energyStorage instanceof BetterEnergyStorage betterEnergyStorage)
                     betterEnergyStorage.copyFrom(this.energyStorage);
                 else
                     EnergyProtectiveFields.LOGGER.warn("Can't update energy capability. " + BetterEnergyStorage.class.getSimpleName() + " not found!");
             }
         });
         
         context.setPacketHandled(true);
         return true;
    }
}
