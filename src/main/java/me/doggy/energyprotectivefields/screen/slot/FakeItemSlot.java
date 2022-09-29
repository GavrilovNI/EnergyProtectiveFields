package me.doggy.energyprotectivefields.screen.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class FakeItemSlot extends SlotItemHandlerWithNotifier
{
    public FakeItemSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @NotNull
    @Override
    public ItemStack getItem()
    {
        return super.getItem();
    }
    
    @Override
    public void onTake(Player pPlayer, ItemStack pStack)
    {
        set(ItemStack.EMPTY);
        super.onTake(pPlayer, pStack);
    }
    
    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return getItem().isEmpty() && super.mayPlace(stack);
    }
    
    @Override
    public ItemStack safeInsert(ItemStack itemStack, int maxCount)
    {
        super.safeInsert(itemStack.copy(), maxCount);
        return itemStack;
    }
    
    @Override
    public ItemStack safeTake(int count, int maxCount, Player p_150650_)
    {
        set(ItemStack.EMPTY);
        return ItemStack.EMPTY;
    }
}
