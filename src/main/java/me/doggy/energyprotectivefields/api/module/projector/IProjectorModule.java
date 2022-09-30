package me.doggy.energyprotectivefields.api.module.projector;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.module.IModule;

public interface IProjectorModule extends IModule
{
    void apply(ModuleInfo<IProjectorModule> moduleModuleInfo, IFieldProjector projector);
    void cancel(ModuleInfo<IProjectorModule> moduleModuleInfo, IFieldProjector projector);
}
