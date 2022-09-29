package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.utils.Vec2i;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class BaseItemInventoryMenu extends BetterMenu
{
    protected BaseItemInventoryMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Vec2i playerInventoryStart, Vec2i playerHotBarStart)
    {
        super(pMenuType, pContainerId);
    
        addPlayerHotbar(playerInventory, playerHotBarStart.getX(), playerHotBarStart.getY());
        addPlayerInventory(playerInventory, playerInventoryStart.getX(), playerInventoryStart.getY());
    }
    
    protected BaseItemInventoryMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Vec2i playerInventoryStart)
    {
        this(pMenuType, pContainerId, playerInventory, playerInventoryStart, playerInventoryStart.above(58));
    }
    
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    
    @Override
    public ItemStack quickMoveStack(Player pPLayer, int pIndex)
    {
        Slot sourceSlot = slots.get(pIndex);
        int countToTake = sourceSlot.getItem().getCount();
        ItemStack sourceStack = sourceSlot.safeTake(countToTake, countToTake, pPLayer);
        ItemStack sourceStackCopy = sourceStack.copy();
        
        boolean fromPlayerInventory = pIndex < VANILLA_SLOT_COUNT;
        
        boolean moved;
        
        int startIndex;
        int endIndex;
        
        if(fromPlayerInventory)
        {
            startIndex = VANILLA_SLOT_COUNT;
            endIndex = slots.size();
        }
        else
        {
            startIndex = 0;
            endIndex = VANILLA_SLOT_COUNT;
        }
        
        moved = moveItemStackTo(sourceStack, startIndex, endIndex, false);
    
        var currentItem = sourceSlot.getItem();
        if(currentItem.isEmpty())
            currentItem = sourceStack;
        else if(currentItem.is(sourceStack.getItem()))
            currentItem.setCount(currentItem.getCount() + sourceStack.getCount());
        else
            pPLayer.drop(sourceStack, true);
    
        sourceSlot.set(currentItem);
    
        if (sourceStack.getCount() == 0)
            sourceSlot.set(ItemStack.EMPTY);
    
        if(moved)
        {
            sourceSlot.onTake(pPLayer, new ItemStack(sourceStackCopy.getItem(), sourceStackCopy.getCount() - sourceStack.getCount()));
            return sourceStackCopy;
        }
        else
        {
            return ItemStack.EMPTY;
        }
    }
    
    protected void addPlayerInventory(Inventory playerInventory, int startX, int startY)
    {
        for(int y = 0; y < PLAYER_INVENTORY_ROW_COUNT; y++)
            for(int x = 0; x < PLAYER_INVENTORY_COLUMN_COUNT; x++)
                addSlot(new Slot(playerInventory, x + y * 9 + 9, startX + x * 18, startY +y * 18));
    }
    
    protected void addPlayerHotbar(Inventory playerInventory, int startX, int startY)
    {
        for(int i = 0; i < HOTBAR_SLOT_COUNT; i++)
            addSlot(new Slot(playerInventory, i, startX+i*18, startY));
    }
}
