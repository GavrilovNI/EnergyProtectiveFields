package me.doggy.energyprotectivefields.api.capability.item;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.api.utils.ItemStackConverter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class ModulesItemStackHandler extends ItemStackHandler implements IHaveDirectionalSlots
{
    public ModulesItemStackHandler(int size)
    {
        super(size);
    }
    
    @Override
    public @Nullable Direction getSlotDirection(int slot)
    {
        return null;
    }
    
    private Optional<Integer> tryFindLimit(ItemStack itemStack)
    {
        IFieldModule module = ItemStackConverter.getStackAs(itemStack, IFieldModule.class);
        if(module != null)
            return Optional.of(module.getLimitInMachineSlot(itemStack));
        return Optional.empty();
    }
    
    @Deprecated // use getStackLimit instead
    @Override
    public int getSlotLimit(int slot)
    {
        var itemStack = getStackInSlot(slot);
        return tryFindLimit(itemStack).orElse(super.getSlotLimit(slot));
    }
    
    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack)
    {
        return tryFindLimit(stack).orElse(super.getStackLimit(slot, stack));
    }
    
    public <T extends IModule> ModuleInfo<T> getModuleInfo(int slot, Class<T> clazz)
    {
        var moduleStack = getStackInSlot(slot);
        return ModuleInfo.get(moduleStack, clazz, getSlotDirection(slot));
    }
    
    public <T extends IModule> ArrayList<ModuleInfo<T>> getModulesInfo(Class<T> clazz)
    {
        ArrayList<ModuleInfo<T>> modules = new ArrayList<>();
        
        for(int i = 0; i < getSlots(); ++i)
        {
            var info = getModuleInfo(i, clazz);
            if(info != null)
                modules.add(info);
        }
        return modules;
    }
}
