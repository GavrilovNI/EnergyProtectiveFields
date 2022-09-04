package me.doggy.energyprotectivefields.api.utils;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.capability.item.IHaveDirectionalSlots;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.items.IItemHandler;

import java.util.ArrayList;

public class InventoryHelper
{
    public static <T> T getStackAs(ItemStack itemStack, Class<T> clazz)
    {
        ItemLike itemLike = itemStack.getItem();
        if(clazz.isAssignableFrom(itemLike.getClass()))
            return (T)itemLike;
        if(itemLike instanceof BlockItem blockItem)
        {
            var block = blockItem.getBlock();
            if(clazz.isAssignableFrom(block.getClass()))
                return (T)block;
        }
        return null;
    }
    
    public static <I extends IItemHandler, T extends IModule> ModuleInfo<T> getModuleInfo(I stackHandler, int slot, Class<T> clazz)
    {
        var moduleStack = stackHandler.getStackInSlot(slot);
        var module = InventoryHelper.getStackAs(moduleStack, clazz);
        return module == null ? null : new ModuleInfo<T>(module, moduleStack.getCount());
    }
    
    public static <I extends IItemHandler, T extends IModule> ArrayList<ModuleInfo<T>> getModuleInfos(I stackHandler, Class<T> clazz)
    {
        ArrayList<ModuleInfo<T>> modules = new ArrayList<>();
        
        for(int i = 0; i < stackHandler.getSlots(); ++i)
        {
            var info = getModuleInfo(stackHandler, i, clazz);
            if(info != null)
                modules.add(info);
        }
        return modules;
    }
    
    public static <I extends IItemHandler & IHaveDirectionalSlots, T extends IModule> ModuleInfo<T> getDirectionalModuleInfo(I stackHandler, int slot, Class<T> clazz)
    {
        var moduleStack = stackHandler.getStackInSlot(slot);
        var module = InventoryHelper.getStackAs(moduleStack, clazz);
        return module == null ? null : new ModuleInfo<T>(module, moduleStack.getCount(), stackHandler.getSlotDirection(slot));
    }
    
    public static <I extends IItemHandler & IHaveDirectionalSlots, T extends IModule> ArrayList<ModuleInfo<T>> getDirectionalModuleInfos(I stackHandler, Class<T> clazz)
    {
        ArrayList<ModuleInfo<T>> modules = new ArrayList<>();
        
        for(int i = 0; i < stackHandler.getSlots(); ++i)
        {
            var info = getDirectionalModuleInfo(stackHandler, i, clazz);
            if(info != null)
                modules.add(info);
        }
        return modules;
    }
}
