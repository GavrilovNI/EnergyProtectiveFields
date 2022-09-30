package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.utils.ItemStackConverter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ModuleInfo<T extends IModule>
{
    private final ItemStack itemStack;
    private final T module;
    @Nullable
    private final Direction slotDirection;
    
    public static <T extends IModule> boolean hasModule(ItemStack itemStack, Class<T> clazz)
    {
        return ItemStackConverter.getStackAs(itemStack, clazz) != null;
    }
    
    @Nullable
    public static <T extends IModule> ModuleInfo<T> get(ItemStack itemStack, Class<T> clazz, @Nullable Direction slotDirection)
    {
        var module = ItemStackConverter.getStackAs(itemStack, clazz);
        return module == null ? null : new ModuleInfo<>(itemStack, module, slotDirection);
    }
    
    protected ModuleInfo(ItemStack itemStack, T module, @Nullable Direction slotDirection)
    {
        this.itemStack = itemStack.copy();
        this.module = module;
        this.slotDirection = slotDirection;
    }
    
    public T getModule()
    {
        return module;
    }
    
    public int getCount()
    {
        return itemStack.getCount();
    }
    
    public ItemStack getItemStack()
    {
        return itemStack.copy();
    }
    
    @Nullable
    public Direction getSlotDirection()
    {
        return slotDirection;
    }
}
