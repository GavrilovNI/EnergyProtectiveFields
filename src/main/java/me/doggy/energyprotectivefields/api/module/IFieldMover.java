package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.Vec3i;

public interface IFieldMover extends IModule
{
    Vec3i getOffset(ShapeBuilder builder);
    
    @Override
    default void applyOnInit(ShapeBuilder builder, int stackSize)
    {
        builder.setCenter(builder.getCenter().offset(getOffset(builder).multiply(stackSize)));
    }
}
