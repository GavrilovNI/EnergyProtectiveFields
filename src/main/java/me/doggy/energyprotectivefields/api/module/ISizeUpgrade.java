package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface ISizeUpgrade extends IShapeModule
{
    int getSizeMultiplier();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        builder.setSize(builder.getSize() + getSizeMultiplier() * moduleInfo.getCount());
    }
}
