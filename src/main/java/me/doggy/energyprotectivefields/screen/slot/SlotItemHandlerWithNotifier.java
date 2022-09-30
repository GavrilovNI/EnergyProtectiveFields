package me.doggy.energyprotectivefields.screen.slot;

import me.doggy.energyprotectivefields.api.capability.item.NotifiableItemStackHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SlotItemHandlerWithNotifier extends SlotItemHandler
{
    private static final Method ON_CONTENTS_CHANGED_METHOD;
    
    static
    {
        Method onContentsChangedMethodLocal = null;
        try
        {
            onContentsChangedMethodLocal = ItemStackHandler.class.getDeclaredMethod("onContentsChanged", int.class);
            onContentsChangedMethodLocal.setAccessible(true);
        }
        catch(NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        ON_CONTENTS_CHANGED_METHOD = onContentsChangedMethodLocal;
    }
    
    ;
    
    
    public SlotItemHandlerWithNotifier(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @Override
    public void setChanged()
    {
        super.setChanged();
        var itemHandler = getItemHandler();
        int slotIndex = getSlotIndex();
        if(itemHandler instanceof NotifiableItemStackHandler stackHandler)
        {
            stackHandler.notifyIfSlotChanged(slotIndex);
        }
        else if(getItemHandler() instanceof ItemStackHandler stackHandler)
        {
            try
            {
                ON_CONTENTS_CHANGED_METHOD.invoke(stackHandler, slotIndex);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }
}
