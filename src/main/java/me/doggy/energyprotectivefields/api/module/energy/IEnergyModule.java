package me.doggy.energyprotectivefields.api.module.energy;

import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.module.IModule;

public interface IEnergyModule extends IModule
{
    void apply(BetterEnergyStorage energyStorage, int count);
}
