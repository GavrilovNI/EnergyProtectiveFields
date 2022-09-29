package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.field.IDirectionalFieldModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.utils.Vec2i;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.api.capability.item.FieldControllerItemStackHandler;
import me.doggy.energyprotectivefields.screen.slot.ModuleSlot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        super(ModMenuTypes.FIELD_CONTROLLER_MENU.get(), pContainerId, inventory, new Vec2i(8, 101), blockEntity);
        this.blockEntity = (FieldControllerBlockEntity) blockEntity;
        this.level = inventory.player.level;
        
        this.blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(itemHandler -> {
            int index = 0;
            this.addSlot(new ModuleSlot(itemHandler, index++, 62, 43, IFieldShape.class));
    
            this.addSlot(new ModuleSlot(itemHandler, index++, 35, 16, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 53, 16, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 71, 16, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 89, 16, IDirectionalFieldModule.class));
            
            this.addSlot(new ModuleSlot(itemHandler, index++, 35, 34, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 89, 34, IDirectionalFieldModule.class));
            
            this.addSlot(new ModuleSlot(itemHandler, index++, 35, 52, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 89, 52, IDirectionalFieldModule.class));
            
            this.addSlot(new ModuleSlot(itemHandler, index++, 35, 70, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 53, 70, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 71, 70, IDirectionalFieldModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 89, 70, IDirectionalFieldModule.class));
    
    
            this.addSlot(new ModuleSlot(itemHandler, index++, 116, 34, IModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 134, 34, IModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 152, 34, IModule.class));
    
            this.addSlot(new ModuleSlot(itemHandler, index++, 116, 52, IModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 134, 52, IModule.class));
            this.addSlot(new ModuleSlot(itemHandler, index++, 152, 52, IModule.class));
        });
    }
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.FIELD_CONTROLLER.get());
    }
}
