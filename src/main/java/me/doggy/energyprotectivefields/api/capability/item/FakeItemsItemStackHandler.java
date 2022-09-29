package me.doggy.energyprotectivefields.api.capability.item;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class FakeItemsItemStackHandler extends ItemStackHandler
{
    public FakeItemsItemStackHandler(int size)
    {
        super(size);
    }
    
    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack)
    {
        return 1;
    }
    
    @Override
    public int getSlotLimit(int slot)
    {
        return 1;
    }
    
    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if(simulate)
        {
            return super.extractItem(slot, amount, true);
        }
        else
        {
            validateSlotIndex(slot);
            if(stacks.get(slot).isEmpty() == false)
            {
                this.stacks.set(slot, ItemStack.EMPTY);
                onContentsChanged(slot);
            }
        }
        return ItemStack.EMPTY;
    }
    
    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
    {
        if(simulate)
        {
            return super.insertItem(slot, stack, true);
        }
        else
        {
            if(stack.isEmpty() == false)
            {
                validateSlotIndex(slot);
    
                if(stacks.get(slot).isEmpty())
                    setStackInSlot(slot, stack);
            }
        }
        
        
        return stack.copy();
    }
    
    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack)
    {
        validateSlotIndex(slot);
    
        var stackToSet = stack.copy();
        stackToSet.setCount(1);
        stacks.set(slot, stackToSet);
        onContentsChanged(slot);
    }
}
