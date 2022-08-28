package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.world.item.ItemStack;

public interface IFieldShape extends IShapeModule
{
    void addFields(ShapeBuilder shapeBuilder);
    
    @Override
    default int getLimitInControllerSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo){}
}
