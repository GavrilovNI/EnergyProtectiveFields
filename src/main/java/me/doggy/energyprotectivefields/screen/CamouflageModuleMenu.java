package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.utils.Vec2i;
import me.doggy.energyprotectivefields.screen.slot.FakeItemSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.items.IItemHandler;

public class CamouflageModuleMenu extends ItemStackInventoryMenu
{
    private static final Vec2i INVENTORY_POS = new Vec2i(8, 86);
    
    public CamouflageModuleMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData)
    {
        super(pContainerId, inventory, INVENTORY_POS, extraData);
    }
    
    public CamouflageModuleMenu(int pContainerId, Inventory inventory, int slotIndex)
    {
        super(pContainerId, inventory, INVENTORY_POS, slotIndex);
    }
    
    @Override
    protected void addSlotsFromItemStack(IItemHandler itemHandler)
    {
        this.addSlot(new FakeItemSlot(itemHandler, 0, 80, 36));
    }
}
