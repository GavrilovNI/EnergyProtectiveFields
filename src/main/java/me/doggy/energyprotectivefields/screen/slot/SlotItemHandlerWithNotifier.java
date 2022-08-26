package me.doggy.energyprotectivefields.screen.slot;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.lang.reflect.InvocationTargetException;

public class SlotItemHandlerWithNotifier extends SlotItemHandler
{
    
    public SlotItemHandlerWithNotifier(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @Override
    public void setChanged()
    {
        super.setChanged();
        if(getItemHandler() instanceof ItemStackHandler itemStackHandler)
        {
            try
            {
                var method = ItemStackHandler.class.getDeclaredMethod("onContentsChanged", int.class);
                method.setAccessible(true);
                method.invoke(itemStackHandler, this.getSlotIndex());
            }
            catch(NoSuchMethodException|InvocationTargetException|IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }
}
