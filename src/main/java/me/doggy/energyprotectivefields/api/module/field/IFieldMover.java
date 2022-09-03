package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.Vec3i;

public interface IFieldMover extends IDirectionalFieldModule
{
    Vec3i getOffset(ShapeBuilder builder, ModuleInfo moduleInfo);
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        builder.setCenter(builder.getCenter().offset(getOffset(builder, moduleInfo).multiply(moduleInfo.getCount())));
    }
}
