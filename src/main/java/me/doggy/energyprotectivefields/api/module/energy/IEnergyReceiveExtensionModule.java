package me.doggy.energyprotectivefields.api.module.energy;

import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;

public interface IEnergyReceiveExtensionModule extends IEnergyModule
{
    int getEnergyReceiveShift();
    
    @Override
    default void apply(BetterEnergyStorage energyStorage, int count)
    {
        int oldMaxReceive = energyStorage.getMaxEnergyStored();
        long shift = ((long)getEnergyReceiveShift()) * count;
        int newMaxReceive = oldMaxReceive + (int)Math.min(Integer.MAX_VALUE - oldMaxReceive, shift);
        energyStorage.setMaxReceive(newMaxReceive);
    }
}
