package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;

public interface IFieldProjector
{
    int getEnergyToBuildEnergyField(BlockPos blockPos);
    int getEnergyToSupportEnergyField(BlockPos blockPos);
    
    boolean canBuildEnergyField(BlockPos blockPos);
    boolean canSupportEnergyField(BlockPos blockPos);
    
    void onBuiltEnergyField(BlockPos blockPos);
    void onSupportedEnergyField(BlockPos blockPos);
}
