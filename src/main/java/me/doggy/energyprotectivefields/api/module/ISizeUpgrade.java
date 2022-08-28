package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface ISizeUpgrade extends IModule
{
    int getSizeMultiplier();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, int stackSize)
    {
        builder.setSize(builder.getSize() + getSizeMultiplier() * stackSize);
    }
}
