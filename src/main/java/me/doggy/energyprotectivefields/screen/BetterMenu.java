package me.doggy.energyprotectivefields.screen;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class BetterMenu extends AbstractContainerMenu
{
    protected BetterMenu(@Nullable MenuType<?> pMenuType, int pContainerId)
    {
        super(pMenuType, pContainerId);
    }
    
    @Override
    protected boolean moveItemStackTo(ItemStack pStack, int pStartIndex, int pEndIndex, boolean pReverseDirection)
    {
        var oldStackCopy = pStack.copy();
        int i = pReverseDirection ? pEndIndex - 1 : pStartIndex;
        
        if (pStack.isStackable())
        {
            while(!pStack.isEmpty())
            {
                if(pReverseDirection ? i < pStartIndex : i >= pEndIndex)
                    break;
                
                Slot slot = this.slots.get(i);
                ItemStack itemstack = slot.getItem();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(pStack, itemstack))
                    slot.safeInsert(pStack);
                
                i += pReverseDirection ? -1 : 1;
            }
        }
        
        if (!pStack.isEmpty())
        {
            i = pReverseDirection ? pEndIndex - 1 : pStartIndex;
            
            while(!pStack.isEmpty())
            {
                if(pReverseDirection ? i < pStartIndex : i >= pEndIndex)
                    break;
                
                Slot slot1 = this.slots.get(i);
                slot1.safeInsert(pStack);
    
                i += pReverseDirection ? -1 : 1;
            }
        }
        
        boolean changed = pStack.getItem() != oldStackCopy.getItem() || pStack.getCount() != oldStackCopy.getCount();
        
        return changed;
    }
}
