package me.doggy.energyprotectivefields.api.module;

import net.minecraft.world.item.ItemStack;

public interface IModule
{
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return itemStack.getMaxStackSize();
    }
}
