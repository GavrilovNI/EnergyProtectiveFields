package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.ILinkingCard;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.utils.ItemStackConvertor;
import me.doggy.energyprotectivefields.block.FieldControllerBlock;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldProjectorBlockEntity extends BlockEntity implements MenuProvider, IFieldProjector
{
    public static final int SLOT_CONTROLLER_LINKER = 0;
    public static final int ITEM_CAPABILITY_SIZE = 1;
    
    public static final int MAX_ENERGY_CAPACITY = 50000;
    public static final int MAX_ENERGY_RECEIVE = 5000;
    
    public static final int ENERGY_TO_BUILD = 20;
    public static final int ENERGY_TO_SUPPORT = 4;
    
    
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(ITEM_CAPABILITY_SIZE)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if(level.isClientSide() == false)
                onInventoryContentChanged(slot);
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
    
    private final BetterEnergyStorage energyStorage = new BetterEnergyStorage(0, MAX_ENERGY_CAPACITY, MAX_ENERGY_RECEIVE, 0)
    {
        @Override
        public void onChanged()
        {
            super.onChanged();
            setChanged();
        }
    };
    
    private LazyOptional<BetterEnergyStorage> lazyEnergyStorage = LazyOptional.empty();
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    
    private WorldLinks.LinkInfo linkedControllerInfo = null;
    
    public FieldProjectorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_PROJECTOR.get(), pWorldPosition, pBlockState);
    }
    
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    @Nullable
    private ILinkingCard getLinker()
    {
        var controllerLinkerStack = itemStackHandler.getStackInSlot(SLOT_CONTROLLER_LINKER);
        var controllerLinker = ItemStackConvertor.getAs(controllerLinkerStack, ILinkingCard.class);
        return controllerLinker;
    }
    
    private FieldControllerBlockEntity getController(boolean onlyIfChunkIsLoaded)
    {
        var controllerLinkerStack = itemStackHandler.getStackInSlot(SLOT_CONTROLLER_LINKER);
        var controllerLinker = ItemStackConvertor.getAs(controllerLinkerStack, ILinkingCard.class);
        if(controllerLinker == null)
            return null;
        return (controllerLinker == null ? null : controllerLinker.findLinkedController(controllerLinkerStack, level, onlyIfChunkIsLoaded));
    }
    
    private void link(@Nullable WorldLinks.LinkInfo connectionInfo)
    {
        unlink();
        if(level instanceof ServerLevel serverLevel)
        {
            linkedControllerInfo = connectionInfo;
            if(connectionInfo != null)
                WorldLinks.get(serverLevel).addLink(connectionInfo, worldPosition);
        }
    }
    
    private void unlink()
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
    
    private void updateControllerFromLinker()
    {
        var controllerLinkerStack = itemStackHandler.getStackInSlot(SLOT_CONTROLLER_LINKER);
        var controllerLinker = ItemStackConvertor.getAs(controllerLinkerStack, ILinkingCard.class);
        if(controllerLinker == null)
            unlink();
        else
            link(controllerLinker.getConnectionInfo(controllerLinkerStack));
    }
    
    private void onInventoryContentChanged(int slot)
    {
        if(slot == SLOT_CONTROLLER_LINKER)
            updateControllerFromLinker();
    }
    
    
    public void onDestroyed()
    {
        unlink();
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
    
    @Override
    public int getEnergyToBuildEnergyField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 36)
        {
            return ENERGY_TO_BUILD;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(ENERGY_TO_BUILD * (distance - 5));
        }
    }
    
    @Override
    public int getEnergyToSupportEnergyField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 36)
        {
            return ENERGY_TO_SUPPORT;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(ENERGY_TO_SUPPORT * (distance - 5));
        }
    }
    
    @Override
    public boolean canBuildEnergyField(BlockPos blockPos)
    {
        return isEnabled() && energyStorage.getEnergyStored() > getEnergyToBuildEnergyField(blockPos);
    }
    
    @Override
    public boolean canSupportEnergyField(BlockPos blockPos)
    {
        return isEnabled() && energyStorage.getEnergyStored() > getEnergyToSupportEnergyField(blockPos);
    }
    
    @Override
    public void onBuiltEnergyField(BlockPos blockPos)
    {
        energyStorage.consumeEnergy(getEnergyToBuildEnergyField(blockPos), false);
    }
    
    @Override
    public void onSupportedEnergyField(BlockPos blockPos)
    {
        energyStorage.consumeEnergy(getEnergyToSupportEnergyField(blockPos), false);
    }
}
