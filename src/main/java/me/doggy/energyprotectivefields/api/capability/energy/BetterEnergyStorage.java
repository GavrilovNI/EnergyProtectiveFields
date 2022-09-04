package me.doggy.energyprotectivefields.api.capability.energy;


import me.doggy.energyprotectivefields.api.INetSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Objects;

public class BetterEnergyStorage implements IEnergyStorage, INetSerializable, INBTSerializable<CompoundTag>
{
    protected int energy;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;
    
    public BetterEnergyStorage()
    {
        this(0, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    public BetterEnergyStorage(int energy, int capacity, int maxReceive, int maxExtract)
    {
        this.capacity = Math.max(capacity, 0);
        this.energy = Math.min(Math.max(energy, 0), capacity);
        this.maxReceive = Math.max(maxReceive, 0);
        this.maxExtract = Math.max(maxExtract, 0);
    }
    
    public BetterEnergyStorage(BetterEnergyStorage betterEnergyStorage)
    {
        copyFrom(betterEnergyStorage);
    }
    
    public BetterEnergyStorage clone()
    {
        return new BetterEnergyStorage(energy, capacity, maxReceive, maxExtract);
    }
    
    public void copyFrom(BetterEnergyStorage energyStorage)
    {
        this.capacity = energyStorage.getMaxEnergyStored();
        this.energy = energyStorage.getEnergyStored();
        this.maxReceive = energyStorage.getMaxReceive();
        this.maxExtract = energyStorage.getMaxExtract();
        onChanged();
    }
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!canReceive())
            return 0;
        if(maxReceive <= 0)
            return 0;
    
        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate)
        {
            energy += energyReceived;
            onChanged();
        }
        return energyReceived;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!canExtract())
            return 0;
        if(maxExtract <= 0)
            return 0;
    
        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate)
        {
            energy -= energyExtracted;
            onChanged();
        }
        return energyExtracted;
    }
    
    public int consumeEnergy(int maxConsume, boolean simulate)
    {
        if(maxConsume <= 0)
            return 0;
        
        int energyConsumed = Math.min(energy, maxConsume);
        if (!simulate)
        {
            energy -= energyConsumed;
            onChanged();
        }
        return energyConsumed;
    }
    
    public int produceEnergy(int maxProduce, boolean simulate)
    {
        if(maxProduce <= 0)
            return 0;
        
        int energyProduced = Math.min(capacity - energy, maxProduce);
        if (!simulate)
        {
            energy += energyProduced;
            onChanged();
        }
        return energyProduced;
    }
    
    
    public boolean consumeExact(int count)
    {
        if(count < 0)
            return false;
        
        if(count <= energy)
        {
            energy -= count;
            onChanged();
            return true;
        }
        return false;
    }
    
    public boolean produceExact(int count)
    {
        if(count < 0)
            return false;
        
        if(count <= capacity - energy)
        {
            energy += count;
            onChanged();
            return true;
        }
        return false;
    }
    
    public boolean extractExact(int count)
    {
        if (!canExtract())
            return false;
        if(count < 0)
            return false;
    
        int maxCanExtract = Math.min(energy, Math.min(this.maxExtract, count));
        if(count < maxCanExtract)
        {
            energy -= count;
            onChanged();
            return true;
        }
        return false;
    }
    
    public boolean receiveExact(int count)
    {
        if (!canReceive())
            return false;
        if(count < 0)
            return false;
    
        int maxCanReceive = Math.min(capacity - energy, Math.min(this.maxReceive, count));
        if(count < maxCanReceive)
        {
            energy += count;
            onChanged();
            return true;
        }
        return false;
    }
    
    public void setEnergyStored(int energy)
    {
        this.energy = Math.min(Math.max(energy, 0), capacity);
        onChanged();
    }
    
    public void setMaxEnergyStored(int capacity)
    {
        this.capacity = Math.max(capacity, 0);
        this.energy = Math.min(this.energy, this.capacity);
        onChanged();
    }
    
    public void setMaxReceive(int maxReceive)
    {
        this.maxReceive = Math.max(maxReceive, 0);
        onChanged();
    }
    
    public void setMaxExtract(int maxExtract)
    {
        this.maxExtract = Math.max(maxExtract, 0);
        onChanged();
    }
    
    public int getMaxReceive()
    {
        return maxReceive;
    }
    
    public int getMaxExtract()
    {
        return maxExtract;
    }
    
    @Override
    public int getEnergyStored()
    {
        return energy;
    }
    
    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }
    
    @Override
    public boolean canExtract()
    {
        return this.maxExtract > 0;
    }
    
    @Override
    public boolean canReceive()
    {
        return this.maxReceive > 0;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        BetterEnergyStorage that = (BetterEnergyStorage)o;
        return energy == that.energy && capacity == that.capacity && maxReceive == that.maxReceive && maxExtract == that.maxExtract;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(energy, capacity, maxReceive, maxExtract);
    }
    
    public void onChanged()
    {
    
    }
    
    @Override
    public void serializeNet(FriendlyByteBuf buf)
    {
        buf.writeInt(energy);
        buf.writeInt(capacity);
        buf.writeInt(maxReceive);
        buf.writeInt(maxExtract);
    }
    
    @Override
    public void deserializeNet(FriendlyByteBuf buf)
    {
        energy = buf.readInt();
        capacity = buf.readInt();
        maxReceive = buf.readInt();
        maxExtract = buf.readInt();
        onChanged();
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("energy", energy);
        nbt.putInt("capacity", capacity);
        nbt.putInt("maxReceive", maxReceive);
        nbt.putInt("maxExtract", maxExtract);
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        energy = nbt.getInt("energy");
        capacity = nbt.getInt("capacity");
        maxReceive = nbt.getInt("maxReceive");
        maxExtract = nbt.getInt("maxExtract");
    }
}
