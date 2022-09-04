package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.world.item.ItemStack;

public interface IFieldShape extends IFieldModule
{
    void addFields(ShapeBuilder shapeBuilder);
    
    @Override
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo){}
}
