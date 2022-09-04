package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.ILinkingCard;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyModule;
import me.doggy.energyprotectivefields.api.utils.Vec2i;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.FieldProjectorBlockEntity;
import me.doggy.energyprotectivefields.screen.slot.ModuleSlot;
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
        super(ModMenuTypes.FIELD_PROJECTOR_MENU.get(), pContainerId, inventory, new Vec2i(8, 92), blockEntity);
        this.blockEntity = (FieldProjectorBlockEntity) blockEntity;
        this.level = inventory.player.level;
        
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            int i = 0;
            this.addSlot(new ModuleSlot(itemHandler, i++, 80, 36, ILinkingCard.class));
            this.addSlot(new ModuleSlot(itemHandler, i++, 152, 18, IEnergyModule.class));
            this.addSlot(new ModuleSlot(itemHandler, i++, 152, 36, IEnergyModule.class));
            this.addSlot(new ModuleSlot(itemHandler, i++, 152, 54, IEnergyModule.class));
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
