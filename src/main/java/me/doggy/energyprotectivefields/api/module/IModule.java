package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.world.item.ItemStack;

public interface IModule
{
    void applyOnInit(ShapeBuilder builder, int stackSize);
    
    default int getLimitInControllerSlot(ItemStack itemStack)
    {
        return itemStack.getItem().getItemStackLimit(itemStack);
    }
}
