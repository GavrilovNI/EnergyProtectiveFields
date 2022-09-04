package me.doggy.energyprotectivefields.api.module.energy;

import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;

import java.util.Collection;

public interface ICombinedEnergyModule extends IEnergyModule
{
    Collection<IEnergyModule> getModules();
    
    @Override
    default void apply(BetterEnergyStorage energyStorage, int count)
    {
        for(var module : getModules())
            module.apply(energyStorage, count);
    }
}
