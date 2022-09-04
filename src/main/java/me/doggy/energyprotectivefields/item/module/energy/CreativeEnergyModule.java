package me.doggy.energyprotectivefields.item.module.energy;

import me.doggy.energyprotectivefields.api.module.energy.ICombinedEnergyModule;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyCapacityExtensionModule;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyModule;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyReceiveExtensionModule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CreativeEnergyModule extends Item implements ICombinedEnergyModule
{
    private static final List<IEnergyModule> modules = List.of(
            new IEnergyCapacityExtensionModule()
            {
                @Override
                public int getEnergyCapacityShift()
                {
                    return Integer.MAX_VALUE;
                }
            },
            new IEnergyReceiveExtensionModule()
            {
                @Override
                public int getEnergyReceiveShift()
                {
                    return Integer.MAX_VALUE;
                }
            }
    );
    
    public CreativeEnergyModule(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public Collection<IEnergyModule> getModules()
    {
        return modules;
    }
    
    @Override
    public int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
}
