package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.world.item.ItemStack;

public interface IPassiveFieldModule extends IFieldModule
{
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
    
    }
    
    @Override
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
}
