package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.FieldSet;
import me.doggy.energyprotectivefields.api.ILinkingCard;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorageWithStats;
import me.doggy.energyprotectivefields.api.utils.ItemStackConvertor;
import me.doggy.energyprotectivefields.block.FieldProjectorBlock;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.data.WorldLinks;
import me.doggy.energyprotectivefields.screen.FieldProjectorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldProjectorBlockEntity extends AbstractFieldProjectorBlockEntity implements MenuProvider
{
    public static final int SLOT_CONTROLLER_LINKER = 0;
    public static final int ITEM_CAPABILITY_SIZE = 1;
    
    public static final int DEFAULT_ENERGY_CAPACITY = 50000;
    public static final int DEFAULT_MAX_ENERGY_RECEIVE = 10000;
    
    private WorldLinks.LinkInfo linkedControllerInfo = null;
    
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(ITEM_CAPABILITY_SIZE)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if(slot == SLOT_CONTROLLER_LINKER)
                updateControllerFromLinker();
        }
        
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack itemStack)
        {
            if(itemStack.isEmpty())
                return true;
            
            var classNeeded = switch(slot)
                    {
                        case SLOT_CONTROLLER_LINKER -> ILinkingCard.class;
                        default -> null;
                    };
            
            if(classNeeded == null)
                return false;
            
            return ItemStackConvertor.getAs(itemStack, classNeeded) != null;
        }
        
        @Override
        public int getSlotLimit(int slot)
        {
            if(slot == SLOT_CONTROLLER_LINKER)
                return 1;
            return super.getSlotLimit(slot);
        }
    };
    private final BetterEnergyStorageWithStats energyStorage = new BetterEnergyStorageWithStats(0, DEFAULT_ENERGY_CAPACITY,
            DEFAULT_MAX_ENERGY_RECEIVE, 0)
    {
        @Override
        public void onChanged()
        {
            super.onChanged();
            setChanged();
        }
    };
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    private LazyOptional<BetterEnergyStorage> lazyEnergyStorage = LazyOptional.empty();
    
    public FieldProjectorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_PROJECTOR.get(), pWorldPosition, pBlockState);
    }
    
    @Override
    public void onEnabled()
    {
        super.onEnabled();
        var controller = getLinkedController();
        if(controller != null)
            controller.onProjectorEnabled(this);
    }
    
    @Override
    public void onDisabled()
    {
        super.onDisabled();
        var controller = getLinkedController();
        if(controller != null)
            controller.onProjectorDisabled(this);
    }
    
    @Override
    public void onControllerDisabled()
    {
        var controller = getLinkedController();
        if(controller != null && controller.isEnabled() == false)
            requestToDestroyAllCreatedFields();
    }
    
    @Override
    public void onControllerEnabled()
    {
        var controller = getLinkedController();
        if(controller != null && controller.isEnabled())
            fieldsToDestroy.removeAll(fields.getFields(FieldSet.FieldState.Created, FieldSet.FieldState.Creating));
    }
    
    private void updateControllerFromLinker()
    {
        var controllerLinkerStack = itemStackHandler.getStackInSlot(SLOT_CONTROLLER_LINKER);
        var controllerLinker = ItemStackConvertor.getAs(controllerLinkerStack, ILinkingCard.class);
        if(controllerLinker == null)
            unlink();
        else
            link(controllerLinker.getConnectionInfo(controllerLinkerStack));
    }
    
    @Nullable
    public FieldControllerBlockEntity getLinkedController()
    {
        if(linkedControllerInfo == null)
            return null;
        if(level.getBlockEntity(linkedControllerInfo.blockPos) instanceof FieldControllerBlockEntity controller &&
                controller.getUUID().equals(linkedControllerInfo.uuid))
        {
            return controller;
        }
        return null;
    }
    public boolean isMyController(FieldControllerBlockEntity controller)
    {
        return linkedControllerInfo != null && controller.getBlockPos().equals(linkedControllerInfo.blockPos) && controller.getUUID().equals(linkedControllerInfo.uuid);
    }
    
    protected void link(@Nullable WorldLinks.LinkInfo connectionInfo)
    {
        unlink();
        if(level instanceof ServerLevel serverLevel)
        {
            linkedControllerInfo = connectionInfo;
            if(connectionInfo != null)
                WorldLinks.get(serverLevel).addLink(connectionInfo, worldPosition);
        }
    }
    
    protected void unlink()
    {
        if(level instanceof ServerLevel serverLevel)
        {
            if(linkedControllerInfo != null)
            {
                WorldLinks.get(serverLevel).removeLink(linkedControllerInfo, worldPosition);
                linkedControllerInfo = null;
            }
        }
    }
    
    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        dropInventory();
        unlink();
    }
    
    @Override
    protected BetterEnergyStorage getEnergyStorage()
    {
        return energyStorage;
    }
    
    @Override
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldProjectorBlock.ENABLED);
    }
    
    @Override
    protected boolean canWork()
    {
        var controller = getLinkedController();
        return isEnabled() && controller != null && controller.isEnabled();
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return lazyItemHandler.cast();
        if(cap == CapabilityEnergy.ENERGY)
            return lazyEnergyStorage.cast();
        
        return super.getCapability(cap, side);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
        lazyEnergyStorage = LazyOptional.of(() -> energyStorage);
    
        updateControllerFromLinker();
        link(linkedControllerInfo);
    }
    
    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
        lazyEnergyStorage.invalidate();
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        pTag.put("inventory", itemStackHandler.serializeNBT());
        pTag.put("energy", energyStorage.serializeNBT());
        super.saveAdditional(pTag);
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        energyStorage.deserializeNBT(pTag.getCompound("energy"));
        super.load(pTag);
    }
    
    public void dropInventory()
    {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++)
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        
        Containers.dropContents(level, worldPosition, inventory);
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FieldProjectorBlockEntity blockEntity)
    {
        if(level instanceof ServerLevel serverLevel)
            blockEntity.serverTick(serverLevel, blockPos, blockState);
    }
    
    
    @Override
    public Component getDisplayName()
    {
        var id = ModBlocks.FIELD_PROJECTOR.getId();
        return new TranslatableComponent("block." + id.getNamespace() + "." + id.getPath());
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        return new FieldProjectorMenu(pContainerId, pInventory, this);
    }
    
    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos, BlockState blockState)
    {
        super.serverTick(level, blockPos, blockState);
        energyStorage.clearStats();
    }
}
