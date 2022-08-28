package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface IStrengthUpgrade extends IModule
{
    int getStrengthMultiplier();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, int stackSize)
    {
        builder.setStrength(builder.getStrength() + getStrengthMultiplier() * stackSize);
    }
}
