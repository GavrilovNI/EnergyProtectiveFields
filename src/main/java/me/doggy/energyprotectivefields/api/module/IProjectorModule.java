package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import net.minecraft.world.item.ItemStack;

public interface IProjectorModule extends IModule
{
    void apply(ItemStack moduleStack, IFieldProjector projector);
}
