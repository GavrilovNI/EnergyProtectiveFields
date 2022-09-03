package me.doggy.energyprotectivefields.item.module.energy;

import me.doggy.energyprotectivefields.api.module.energy.IEnergyCapacityExtensionModule;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyReceiveExtensionModule;
import net.minecraft.world.item.Item;

public class EnergyCapacityExtensionModule extends Item implements IEnergyCapacityExtensionModule
{
    private final int energyCapacityUpgrade;
    
    public EnergyCapacityExtensionModule(Properties pProperties, int energyCapacityUpgrade)
    {
        super(pProperties);
        this.energyCapacityUpgrade = energyCapacityUpgrade;
    }
    
    @Override
    public int getEnergyCapacityShift()
    {
        return energyCapacityUpgrade;
    }
}
