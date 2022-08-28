package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface IStrengthUpgrade extends IShapeModule
{
    int getStrengthMultiplier();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        builder.setStrength(builder.getStrength() + getStrengthMultiplier() * moduleInfo.getCount());
    }
}
