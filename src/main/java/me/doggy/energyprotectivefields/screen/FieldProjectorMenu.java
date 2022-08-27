package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.block.entity.FieldProjectorBlockEntity;
import me.doggy.energyprotectivefields.screen.slot.SlotItemHandlerWithNotifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.CapabilityItemHandler;

public class FieldProjectorMenu extends AbstractContainerWithEnergyMenu
{
    private final FieldProjectorBlockEntity blockEntity;
    private final Level level;
    
    public FieldProjectorMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData)
    {
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }
    
    public FieldProjectorMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity)
    {
        super(ModMenuTypes.FIELD_PROJECTOR_MENU.get(), pContainerId, inventory.player, blockEntity);
        checkContainerSize(inventory, 2);
        this.blockEntity = (FieldProjectorBlockEntity) blockEntity;
        this.level = inventory.player.level;
    
        addPlayerHotbar(inventory, 8, 150);
        addPlayerInventory(inventory, 8, 92);
        
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            int i = 0;
            this.addSlot(new SlotItemHandlerWithNotifier(itemHandler, i++, 80, 36));
        });
    }
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.FIELD_PROJECTOR.get());
    }
    
    @Override
    public int getSlotsCount()
    {
        return FieldProjectorBlockEntity.ITEM_CAPABILITY_SIZE;
    }
}
