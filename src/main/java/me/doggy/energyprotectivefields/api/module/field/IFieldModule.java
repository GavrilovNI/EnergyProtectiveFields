package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.IModule;

public interface IFieldModule extends IModule
{
    void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo);
}
