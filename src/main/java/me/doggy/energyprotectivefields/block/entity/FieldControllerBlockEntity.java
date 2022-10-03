package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.*;
import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorageWithStats;
import me.doggy.energyprotectivefields.api.module.energy.IEnergyModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.block.FieldControllerBlock;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.controller.FieldsDistributor;
import me.doggy.energyprotectivefields.controller.IFieldDistributingProjectorChooser;
import me.doggy.energyprotectivefields.controller.IProjectorsProvider;
import me.doggy.energyprotectivefields.controller.ProjectorModulesHelper;
import me.doggy.energyprotectivefields.data.WorldFieldsBounds;
import me.doggy.energyprotectivefields.data.WorldLinks;
import me.doggy.energyprotectivefields.api.capability.item.FieldControllerItemStackHandler;
import me.doggy.energyprotectivefields.screen.FieldControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class FieldControllerBlockEntity extends AbstractFieldProjectorBlockEntity implements IHaveUUID, ILinkable, MenuProvider, IProjectorsProvider, IFieldDistributingProjectorChooser
{
    private static final BetterEnergyStorage defaultEnergyStorage = new BetterEnergyStorage(0, 50000, 10000, 0);
    
    private UUID uuid;
    
    private final HashSet<IFieldProjector> fieldProjectors = new HashSet<>();
    
    private Set<BlockPos> shapePositions = new HashSet<>();
    private ShapeBuilder currentShapeBuilder = null;
    
    private boolean inventoryChanged = false;
    private boolean shapeChanged = false;
    private boolean energyChanged = false;
    
    private final FieldControllerItemStackHandler itemStackHandler = new FieldControllerItemStackHandler()
    {
        @Override
        protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack)
        {
            if(level.isClientSide() == false)
            {
                boolean wasInventoryChanged = inventoryChanged;
                inventoryChanged = true;
                shapeChanged = (wasInventoryChanged && shapeChanged) ||
                        ModuleInfo.hasModule(oldStack, IFieldModule.class) || ModuleInfo.hasModule(newStack, IFieldModule.class);
                energyChanged = (wasInventoryChanged && energyChanged) ||
                        ModuleInfo.hasModule(oldStack, IEnergyModule.class) || ModuleInfo.hasModule(newStack, IEnergyModule.class);
    
                projectorModulesHelper.onItemHandlerSlotChanged(slot, oldStack, newStack);
            }
            setChanged();
        }
    };
    
    private final ProjectorModulesHelper projectorModulesHelper = new ProjectorModulesHelper(itemStackHandler, this);
    private FieldsDistributor fieldsDistributor;
    
    private final BetterEnergyStorageWithStats energyStorage = new BetterEnergyStorageWithStats(defaultEnergyStorage)
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
    
    public FieldControllerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_CONTROLLER.get(), pWorldPosition, pBlockState, defaultEnergyStorage);
        uuid = UUID.randomUUID();
        fieldProjectors.add(this);
    }
    
    protected void onInventoryChanged(boolean forceShapeUpdate, boolean forceEnergyUpdate)
    {
        if(forceShapeUpdate || shapeChanged)
            updateShape();
        if(forceEnergyUpdate || energyChanged)
            updateEnergyStorage(itemStackHandler.getModulesInfo(IEnergyModule.class));
        inventoryChanged = false;
    }
    
    @Nullable
    public BoundingBox getShapeBounds()
    {
        if(currentShapeBuilder == null)
            return null;
        return currentShapeBuilder.getBounds();
    }
    
    public boolean isInsideField(Vec3i pos)
    {
        if(currentShapeBuilder == null)
            return false;
        return currentShapeBuilder.isInsideField(itemStackHandler.getShape(), pos);
    }
    
    @Override
    public IFieldProjector getBestProjector(Set<IFieldProjector> projectors, BlockPos fieldPosition)
    {
        IFieldProjector best = this;
        int minEnergy = best.getEnergyToBuildField(fieldPosition);
        
        for(var projector : projectors)
        {
            if(projector.isEnabled() && projector != this)
            {
                int energyToBuild = projector.getEnergyToBuildField(fieldPosition);
                if(best == null || energyToBuild < minEnergy)
                {
                    best = projector;
                    minEnergy = energyToBuild;
                }
            }
        }
        return best;
    }
    
    protected void updateWorldFieldBounds()
    {
        if(level instanceof ServerLevel serverLevel)
        {
            var worldFieldsBounds = WorldFieldsBounds.get(serverLevel);
            if(isEnabled() && isRemoved() == false)
                worldFieldsBounds.updateController(this);
            else
                worldFieldsBounds.removeController(worldPosition);
        }
    }
    
    protected void removeAllFieldBlocksWhichNotInShapeFromProjectors()
    {
        for(var projector : fieldProjectors)
            projector.removeFieldsIf(blockPos -> shapePositions.contains(blockPos) == false);
    }
    
    private IFieldProjector getFieldProjectorIfMine(FieldBlockEntity fieldBlock)
    {
        var projectorPosition = fieldBlock.getProjectorPosition();
        if(projectorPosition != null)
        {
            var fittingProjectors = fieldProjectors.stream()
                    .filter(p -> p.getPosition().equals(projectorPosition)).collect(Collectors.toSet());
            for(var projector : fittingProjectors)
            {
                if(fieldBlock.isMyProjector(projector))
                    return projector;
            }
        }
        return null;
    }
    
    protected void loadFieldsFromWorld()
    {
        for(var blockPos : shapePositions)
        {
            if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity fieldBlock)
            {
                var projector = getFieldProjectorIfMine(fieldBlock);
                fieldBlock.setProjectorPosition(projector.getPosition());
            }
        }
    }
    
    protected void updateShape()
    {
        IFieldShape fieldShape = itemStackHandler.getShape();
        if(fieldShape != null)
        {
            var modules = itemStackHandler.getModulesInfo(IFieldModule.class);
            currentShapeBuilder = new ShapeBuilder(this, modules).init().addFields(fieldShape);
            shapePositions = currentShapeBuilder.build();
        }
        else
        {
            currentShapeBuilder = null;
            shapePositions = new HashSet<>();
        }
        
        removeAllFieldBlocksWhichNotInShapeFromProjectors();
        HashSet<BlockPos> notDistributedFields = new HashSet<>(shapePositions);
        for(var projector : fieldProjectors)
            notDistributedFields.removeAll(projector.getAllFieldsInShape());
    
        fieldsDistributor.distributeFields(notDistributedFields);
    
        updateWorldFieldBounds();
    }
    
    @Override
    public UUID getUUID()
    {
        return uuid;
    }
    
    public void dropInventory()
    {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++)
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        
        Containers.dropContents(level, worldPosition, inventory);
    }
    
    @Override
    public void onEnabled()
    {
        super.onEnabled();
        for(var projector : fieldProjectors)
            projector.onControllerEnabled();
        
        updateWorldFieldBounds();
    }
    
    @Override
    public void onDisabled()
    {
        super.onDisabled();
        for(var projector : fieldProjectors)
            projector.onControllerDisabled();
        
        updateWorldFieldBounds();
    }
    
    @Override
    public void onControllerEnabled()
    {
    
    }
    
    @Override
    public void onControllerDisabled()
    {
    
    }
    
    @Override
    protected boolean canWork()
    {
        return isEnabled();
    }
    
    @Override
    public BetterEnergyStorage getEnergyStorage()
    {
        return energyStorage;
    }
    
    @Override
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    @Override
    public void onDestroying()
    {
        dropInventory();
    
        updateWorldFieldBounds();
    
        if(level instanceof ServerLevel serverLevel)
            WorldLinks.get(serverLevel).removeLinksByController(this);
        
        super.onDestroying();
    }
    
    @Override
    public Component getDisplayName()
    {
        var id = ModBlocks.FIELD_CONTROLLER.getId();
        return new TranslatableComponent("block." + id.getNamespace() + "." + id.getPath());
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        return new FieldControllerMenu(pContainerId, pInventory, this);
    }
    
    public void onProjectorDisabled(IFieldProjector fieldProjector)
    {
        if(fieldProjector == this)
            return;
        
        if(fieldProjectors.contains(fieldProjector) == false)
            throw new IllegalArgumentException("this controller and projector aren't linked");
    
    
        fieldsDistributor.distributeFieldsFrom(fieldProjector);
    }
    
    public void onProjectorEnabled(IFieldProjector projector)
    {
        if(projector == this)
            return;
        
        if(fieldProjectors.contains(projector) == false)
            throw new IllegalArgumentException("this controller and projector aren't linked");
    
        fieldsDistributor.redistributeFieldsForNew(projector);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
        lazyEnergyStorage = LazyOptional.of(() -> energyStorage);
        
        if(level.isClientSide() == false)
        {
            fieldsDistributor = new FieldsDistributor(this, this, getLevel());
            
            fieldProjectors.clear();
            fieldProjectors.add(this);
            for(var position : WorldLinks.get((ServerLevel)level).getLinks(WorldLinks.getControllerLinkInfo(this)))
            {
                if(level.getBlockEntity(position) instanceof IFieldProjector fieldProjector)
                    fieldProjectors.add(fieldProjector);
            }
            
            onInventoryChanged(true, true);
            
            projectorModulesHelper.onLoad();
    
            loadFieldsFromWorld();
        }
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
        super.saveAdditional(pTag);
        pTag.putUUID("uuid", uuid);
        pTag.put("inventory", itemStackHandler.serializeNBT());
        pTag.put("energy", energyStorage.serializeNBT());
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        uuid = pTag.getUUID("uuid");
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        energyStorage.deserializeNBT(pTag.getCompound("energy"));
        super.load(pTag);
    }
    
    @Override
    public void onLinked(ServerLevel level, BlockPos blockPos)
    {
         if(this.level == level)
        {
            if(level.getBlockEntity(blockPos) instanceof IFieldProjector fieldProjector)
            {
                if(fieldProjectors.contains(fieldProjector))
                    throw new IllegalArgumentException("this controller and projector are already linked");
                
                fieldProjectors.add(fieldProjector);
                if(fieldProjector.isEnabled())
                    fieldsDistributor.redistributeFieldsForNew(fieldProjector);
    
                projectorModulesHelper.onProjectorAdded(fieldProjector);
            }
        }
    }
    
    @Override
    public void onUnlinked(ServerLevel level, BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof IFieldProjector fieldProjector)
        {
            if(fieldProjectors.contains(fieldProjector) == false)
                throw new IllegalArgumentException("this controller and projector aren't linked");
            
            fieldProjectors.remove(fieldProjector);
            fieldsDistributor.distributeFieldsFrom(fieldProjector);
    
            projectorModulesHelper.onProjectorRemoved(fieldProjector);
        }
    }
    
    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos, BlockState blockState)
    {
        itemStackHandler.findChanges();
        
        if(inventoryChanged)
            onInventoryChanged(false, false);
        
        super.serverTick(level, blockPos, blockState);
        energyStorage.clearStats();
    }
    
    @Override
    public Set<IFieldProjector> getProjectors()
    {
        return Collections.unmodifiableSet(fieldProjectors);
    }
}
