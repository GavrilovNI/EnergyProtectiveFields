package me.doggy.energyprotectivefields.api.capability.energy;

import net.minecraft.nbt.CompoundTag;

public class InfinityEnergyStorage extends BetterEnergyStorage
{
    public InfinityEnergyStorage()
    {
        this(Integer.MAX_VALUE);
    }
    
    public InfinityEnergyStorage(int maxExtract)
    {
        super(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, maxExtract);
    }
    
    @Override
    public void setEnergyStored(int energy)
    {
        this.energy = Integer.MAX_VALUE;
    }
    
    @Override
    public void setMaxEnergyStored(int capacity)
    {
        this.capacity = Integer.MAX_VALUE;
    }
    
    @Override
    public void setMaxReceive(int maxReceive)
    {
        this.maxReceive = 0;
    }
    
    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        return 0;
    }
    
    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!canExtract())
            return 0;
        
        return Math.min(this.maxExtract, maxExtract);
    }
    
    @Override
    public int getEnergyStored()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public int getMaxEnergyStored()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public boolean canExtract()
    {
        return this.maxExtract > 0;
    }
    
    @Override
    public boolean canReceive()
    {
        return false;
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("maxExtract", maxExtract);
        return nbt;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        maxExtract = nbt.contains("maxExtract") ? nbt.getInt("maxExtract") : Integer.MAX_VALUE;
    }
}
