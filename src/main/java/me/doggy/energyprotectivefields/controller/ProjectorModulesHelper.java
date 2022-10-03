package me.doggy.energyprotectivefields.controller;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.capability.item.ModulesItemStackHandler;
import me.doggy.energyprotectivefields.api.module.projector.IProjectorModule;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Set;

public class ProjectorModulesHelper
{
    private final ModulesItemStackHandler itemStackHandler;
    private final IProjectorsProvider projectorsProvider;
    
    public ProjectorModulesHelper(ModulesItemStackHandler itemStackHandler, IProjectorsProvider projectorsProvider)
    {
        this.itemStackHandler = itemStackHandler;
        this.projectorsProvider = projectorsProvider;
    }
    
    public void onItemHandlerSlotChanged(int slot, ItemStack oldStack, ItemStack newStack)
    {
        var slotDirection = itemStackHandler.getSlotDirection(slot);
        var oldProjectorModule = ModuleInfo.get(oldStack, IProjectorModule.class, slotDirection);
        var newProjectorModule = ModuleInfo.get(newStack, IProjectorModule.class, slotDirection);
    
        if(oldProjectorModule != null)
            cancelProjectorModule(oldProjectorModule);
    
        if(newProjectorModule != null)
            applyProjectorModule(newProjectorModule);
    }
    
    public void onLoad()
    {
        for(var moduleInfo : getProjectorModules())
            for(var projector : projectorsProvider.getProjectors())
                moduleInfo.getModule().apply(moduleInfo, projector);
    }
    
    public void onProjectorAdded(IFieldProjector projector)
    {
        applyProjectorModules(projector);
    }
    
    public void onProjectorRemoved(IFieldProjector projector)
    {
        cancelProjectorModules(projector);
    }
    
    
    private void applyProjectorModule(ModuleInfo<IProjectorModule> moduleInfo)
    {
        for(var projector : projectorsProvider.getProjectors())
            moduleInfo.getModule().apply(moduleInfo, projector);
    }
    
    private void cancelProjectorModule(ModuleInfo<IProjectorModule> moduleInfo)
    {
        for(var projector : projectorsProvider.getProjectors())
            moduleInfo.getModule().cancel(moduleInfo, projector);
    }
    
    private ArrayList<ModuleInfo<IProjectorModule>> getProjectorModules()
    {
        return itemStackHandler.getModulesInfo(IProjectorModule.class);
    }
    
    private void applyProjectorModules(IFieldProjector projector)
    {
        for(var moduleInfo : getProjectorModules())
            moduleInfo.getModule().apply(moduleInfo, projector);
    }
    
    private void cancelProjectorModules(IFieldProjector projector)
    {
        for(var moduleInfo : getProjectorModules())
            moduleInfo.getModule().cancel(moduleInfo, projector);
    }
}
