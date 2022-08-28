package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;

public interface IShapeModule extends IModule
{
    void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo);
}
