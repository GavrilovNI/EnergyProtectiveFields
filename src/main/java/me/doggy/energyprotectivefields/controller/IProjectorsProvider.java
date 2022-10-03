package me.doggy.energyprotectivefields.controller;

import me.doggy.energyprotectivefields.api.IFieldProjector;

import java.util.Set;

public interface IProjectorsProvider
{
    Set<IFieldProjector> getProjectors();
}
