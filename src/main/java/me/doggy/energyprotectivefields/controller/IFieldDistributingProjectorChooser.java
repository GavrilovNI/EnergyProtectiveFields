package me.doggy.energyprotectivefields.controller;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import net.minecraft.core.BlockPos;

import java.util.Set;

public interface IFieldDistributingProjectorChooser
{
    IFieldProjector getBestProjector(Set<IFieldProjector> projectors, BlockPos fieldPosition);
}
