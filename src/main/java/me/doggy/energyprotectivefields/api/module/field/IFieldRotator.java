package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface IFieldRotator extends IDirectionalFieldModule
{
    int getRotation();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        var direction = moduleInfo.getSlotDirection();
        if(direction != null)
            builder.setRotation(direction, builder.getRotation(direction) + getRotation() * moduleInfo.getCount());
    }
}
