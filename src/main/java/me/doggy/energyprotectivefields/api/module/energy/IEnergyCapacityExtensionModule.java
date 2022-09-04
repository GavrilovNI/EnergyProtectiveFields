package me.doggy.energyprotectivefields.api.module.energy;

import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;

public interface IEnergyCapacityExtensionModule extends IEnergyModule
{
    int getEnergyCapacityShift();
    
    @Override
    default void apply(BetterEnergyStorage energyStorage, int count)
    {
        int oldCapacity = energyStorage.getMaxEnergyStored();
        long shift = ((long)getEnergyCapacityShift()) * count;
        int newCapacity = oldCapacity + (int)Math.min(Integer.MAX_VALUE - oldCapacity, shift);
        energyStorage.setMaxEnergyStored(newCapacity);
    }
}
