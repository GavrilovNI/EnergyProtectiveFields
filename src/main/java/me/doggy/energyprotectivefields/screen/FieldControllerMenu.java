package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.utils.Vec2i;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.screen.slot.SlotItemHandlerWithNotifier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

public class FieldControllerMenu extends AbstractContainerWithEnergyMenu
{
    private final FieldControllerBlockEntity blockEntity;
    private final Level level;
    
    public FieldControllerMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData)
    {
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }
    
    public FieldControllerMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity)
    {
        super(ModMenuTypes.FIELD_CONTROLLER_MENU.get(), pContainerId, inventory, new Vec2i(8, 92), blockEntity);
        this.blockEntity = (FieldControllerBlockEntity) blockEntity;
        this.level = inventory.player.level;
        
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            int i = 0;
            this.addSlot(new SlotItemHandlerWithNotifier(itemHandler, i++, 60, 36));
            this.addSlot(new SlotItemHandlerWithNotifier(itemHandler, i++, 82, 36));
            this.addSlot(new SlotItemHandlerWithNotifier(itemHandler, i++, 100, 36));
        });
    }
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.FIELD_CONTROLLER.get());
    }
    
    @Override
    public int getSlotsCount()
    {
        return FieldControllerBlockEntity.ITEM_CAPABILITY_SIZE;
    }
}
