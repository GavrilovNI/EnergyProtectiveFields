package me.doggy.energyprotectivefields.item.module.energy;

import me.doggy.energyprotectivefields.api.module.energy.IEnergyReceiveExtensionModule;
import net.minecraft.world.item.Item;

public class EnergyReceiveExtensionModule extends Item implements IEnergyReceiveExtensionModule
{
    private final int energyReceiveUpgrade;
    
    public EnergyReceiveExtensionModule(Properties pProperties, int energyReceiveUpgrade)
    {
        super(pProperties);
        this.energyReceiveUpgrade = energyReceiveUpgrade;
    }
    
    @Override
    public int getEnergyReceiveShift()
    {
        return energyReceiveUpgrade;
    }
}
