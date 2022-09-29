package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.utils.Vec2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class ItemStackInventoryMenu extends BaseItemInventoryMenu
{
    protected final int slotIndex;
    protected final ItemStack itemStackWithHandler;
    
    public ItemStackInventoryMenu(int pContainerId, Inventory inventory, Vec2i playerInventoryStart, FriendlyByteBuf extraData)
    {
        this(pContainerId, inventory, playerInventoryStart, extraData.readInt());
    }
    
    public ItemStackInventoryMenu(int pContainerId, Inventory inventory, Vec2i playerInventoryStart, int slotIndex)
    {
        super(ModMenuTypes.CAMOUFLAGE_MODULE_MENU.get(), pContainerId, inventory, playerInventoryStart);
        this.slotIndex = slotIndex;
        this.itemStackWithHandler = inventory.getItem(this.slotIndex);
        
        this.itemStackWithHandler.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(this::addSlotsFromItemStack);
    }
    
    protected abstract void addSlotsFromItemStack(IItemHandler itemHandler);
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return pPlayer.getInventory().getItem(slotIndex).equals(itemStackWithHandler);
    }
}
