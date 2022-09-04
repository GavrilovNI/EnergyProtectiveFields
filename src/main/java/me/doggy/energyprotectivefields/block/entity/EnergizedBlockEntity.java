package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyModule;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;

public abstract class EnergizedBlockEntity extends BlockEntity
{
    private BetterEnergyStorage defaultEnergyStorage;
    
    public EnergizedBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState, BetterEnergyStorage defaultEnergyStorage)
    {
        super(pType, pWorldPosition, pBlockState);
        setDefaultEnergyStorage(defaultEnergyStorage);
    }
    
    public abstract BetterEnergyStorage getEnergyStorage();
    
    public void setDefaultEnergyStorage(BetterEnergyStorage defaultEnergyStorage)
    {
        if(defaultEnergyStorage.equals(this.defaultEnergyStorage) == false)
        {
            this.defaultEnergyStorage = defaultEnergyStorage;
            setChanged();
        }
    }
    
    public void updateEnergyStorage(Collection<ModuleInfo<IEnergyModule>> energyModuleInfos)
    {
        var newEnergyStorage = defaultEnergyStorage.clone();
        
        for(var moduleInfo : energyModuleInfos)
            moduleInfo.getModule().apply(newEnergyStorage, moduleInfo.getCount());
        
        var currentEnergyStorage = getEnergyStorage();
        newEnergyStorage.setEnergyStored(currentEnergyStorage.getEnergyStored());
        
        currentEnergyStorage.copyFrom(newEnergyStorage);
    }
}
