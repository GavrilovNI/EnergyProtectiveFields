package me.doggy.energyprotectivefields.item.module.energy;

import me.doggy.energyprotectivefields.api.module.energy.IEnergyCapacityExtensionModule;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyReceiveExtensionModule;
import net.minecraft.world.item.Item;

public class CreativeEnergyModule extends Item implements IEnergyCapacityExtensionModule, IEnergyReceiveExtensionModule
{
    public CreativeEnergyModule(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public int getEnergyCapacityShift()
    {
        return Integer.MAX_VALUE;
    }
    
    @Override
    public int getEnergyReceiveShift()
    {
        return Integer.MAX_VALUE;
    }
}
