package me.doggy.energyprotectivefields.screen.slot;

import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.utils.ItemStackConverter;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ModuleSlot<T extends IModule> extends SlotItemHandlerWithNotifier
{
    private Class<T> clazz;
    
    public ModuleSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition, Class<T> clazz)
    {
        super(itemHandler, index, xPosition, yPosition);
        this.clazz = clazz;
    }
    
    @Override
    public boolean mayPlace(ItemStack pStack)
    {
        T module = ItemStackConverter.getStackAs(pStack, clazz);
        return module != null && super.mayPlace(pStack);
    }
    
    @Deprecated // use getMaxStackSize(ItemStack pStack)
    @Override
    public int getMaxStackSize()
    {
        return super.getMaxStackSize();
    }
    
    @Override
    public int getMaxStackSize(ItemStack pStack)
    {
        T module = ItemStackConverter.getStackAs(pStack, clazz);
        if(module != null)
            return Math.min(module.getLimitInMachineSlot(pStack), super.getMaxStackSize(pStack));
        return super.getMaxStackSize(pStack);
    }
}
