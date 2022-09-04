package me.doggy.energyprotectivefields.api.energy;


import me.doggy.energyprotectivefields.api.INetSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Objects;

public class BetterEnergyStorageWithStats extends BetterEnergyStorage
{
    protected int receivedEnergy = 0;
    protected int extractedEnergy = 0;
    protected int producedEnergy = 0;
    protected int consumedEnergy = 0;
    
    protected int clearedReceivedEnergy = 0;
    protected int clearedExtractedEnergy = 0;
    protected int clearedProducedEnergy = 0;
    protected int clearedConsumedEnergy = 0;
    
    public BetterEnergyStorageWithStats()
    {
        super();
    }
    
    public BetterEnergyStorageWithStats(int energy, int capacity, int maxReceive, int maxExtract)
    {
        super(energy, capacity, maxReceive, maxExtract);
    }
    
    @Override
    public void copyFrom(BetterEnergyStorage energyStorage)
    {
        super.copyFrom(energyStorage);
        if(energyStorage instanceof BetterEnergyStorageWithStats betterEnergyStorageWithStats)
        {
            receivedEnergy = betterEnergyStorageWithStats.receivedEnergy;
            extractedEnergy = betterEnergyStorageWithStats.extractedEnergy;
            producedEnergy = betterEnergyStorageWithStats.producedEnergy;
            consumedEnergy = betterEnergyStorageWithStats.consumedEnergy;
        }
    }
    
    @Override
    public BetterEnergyStorage copy()
    {
        BetterEnergyStorageWithStats result = new BetterEnergyStorageWithStats();
        result.copyFrom(this);
        return result;
    }
    
    public void clearStats()
    {
        clearedReceivedEnergy = receivedEnergy;
        clearedExtractedEnergy = extractedEnergy;
        clearedProducedEnergy = producedEnergy;
        clearedConsumedEnergy = consumedEnergy;
        
        receivedEnergy = 0;
        extractedEnergy = 0;
        producedEnergy = 0;
        consumedEnergy = 0;
        onChanged();
    }
    
    public int getReceivedEnergy()
    {
        return receivedEnergy;
    }
    
    public int getExtractedEnergy()
    {
        return extractedEnergy;
    }
    
    public int getProducedEnergy()
    {
        return producedEnergy;
    }
    
    public int getConsumedEnergy()
    {
        return consumedEnergy;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        if(!super.equals(o))
            return false;
        BetterEnergyStorageWithStats that = (BetterEnergyStorageWithStats)o;
        return receivedEnergy == that.receivedEnergy && extractedEnergy == that.extractedEnergy && producedEnergy == that.producedEnergy && consumedEnergy == that.consumedEnergy;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), receivedEnergy, extractedEnergy, producedEnergy, consumedEnergy);
    }
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        var receivedEnergy = super.receiveEnergy(maxReceive, simulate);
        if (!simulate)
            this.receivedEnergy += receivedEnergy;
        return receivedEnergy;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        var extractedEnergy = super.extractEnergy(maxExtract, simulate);
        if (!simulate)
            this.extractedEnergy += extractedEnergy;
        return extractedEnergy;
    }
    
    public int consumeEnergy(int maxConsume, boolean simulate)
    {
        var consumedEnergy = super.consumeEnergy(maxConsume, simulate);
        if (!simulate)
            this.consumedEnergy += consumedEnergy;
        return consumedEnergy;
    }
    
    public int produceEnergy(int maxProduce, boolean simulate)
    {
        var producedEnergy = super.produceEnergy(maxProduce, simulate);
        if (!simulate)
            this.producedEnergy += producedEnergy;
        return producedEnergy;
    }
    
    
    public boolean consumeExact(int count)
    {
        var consumed = super.consumeExact(count);
        if(consumed)
            this.consumedEnergy += count;
        return consumed;
    }
    
    public boolean produceExact(int count)
    {
        var produced = super.produceExact(count);
        if(produced)
            this.producedEnergy += count;
        return produced;
    }
    
    public boolean extractExact(int count)
    {
        var extracted = super.extractExact(count);
        if(extracted)
            this.extractedEnergy += count;
        return extracted;
    }
    
    public boolean receiveExact(int count)
    {
        var received = super.receiveExact(count);
        if(received)
            this.receivedEnergy += count;
        return received;
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        var nbt = super.serializeNBT();
    
        CompoundTag stats = new CompoundTag();
        stats.putInt("received", receivedEnergy);
        stats.putInt("extracted", extractedEnergy);
        stats.putInt("produced", producedEnergy);
        stats.putInt("consumed", consumedEnergy);
        nbt.put("stats", stats);
        
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        super.deserializeNBT(nbt);
        CompoundTag stats = nbt.getCompound("stats");
        if(stats != null)
        {
            receivedEnergy = stats.getInt("received");
            extractedEnergy = stats.getInt("extracted");
            producedEnergy = stats.getInt("produced");
            consumedEnergy = stats.getInt("consumed");
        }
    }
    
    @Override
    public void serializeNet(FriendlyByteBuf buf)
    {
        super.serializeNet(buf);
        buf.writeInt(clearedReceivedEnergy);
        buf.writeInt(clearedExtractedEnergy);
        buf.writeInt(clearedProducedEnergy);
        buf.writeInt(clearedConsumedEnergy);
    }
    
    @Override
    public void deserializeNet(FriendlyByteBuf buf)
    {
        super.deserializeNet(buf);
        receivedEnergy = buf.readInt();
        extractedEnergy = buf.readInt();
        producedEnergy = buf.readInt();
        consumedEnergy = buf.readInt();
    }
}
