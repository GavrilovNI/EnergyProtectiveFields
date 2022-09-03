package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.*;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorageWithStats;
import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.block.FieldControllerBlock;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.data.WorldLinks;
import me.doggy.energyprotectivefields.data.handler.FieldControllerItemStackHandler;
import me.doggy.energyprotectivefields.screen.FieldControllerMenu;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FieldControllerBlockEntity extends AbstractFieldProjectorBlockEntity implements IHaveUUID, ILinkable, MenuProvider
{
    public static final int ENERGY_STORAGE_DEFAULT_CAPACITY = 50000;
    public static final int ENERGY_STORAGE_DEFAULT_RECEIVE = 10000;
    
    private UUID uuid;
    
    private boolean hasEnergyInfinityModule;
    
    private final HashSet<IFieldProjector> fieldProjectors = new HashSet<>();
    
    private Set<BlockPos> shapePositions = new HashSet<>();
    
    private final FieldControllerItemStackHandler itemStackHandler = new FieldControllerItemStackHandler()
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if(level.isClientSide() == false)
            {
                updateShape();
                //updateEnergyStorage();
            }
        }
    };
    private final BetterEnergyStorageWithStats energyStorage = new BetterEnergyStorageWithStats(0, ENERGY_STORAGE_DEFAULT_CAPACITY,
            ENERGY_STORAGE_DEFAULT_RECEIVE, 0)
    {
        @Override
        public void onChanged()
        {
            super.onChanged();
            setChanged();
        }
        
        @Override
        public int consumeEnergy(int maxConsume, boolean simulate)
        {
            if(hasEnergyInfinityModule)
            {
                var oldEnergy = energy;
                energy = Integer.MAX_VALUE;
                var result = super.consumeEnergy(maxConsume, simulate);
                energy = oldEnergy;
                return result;
            }
            else
            {
                return super.consumeEnergy(maxConsume, simulate);
            }
        }
        
        @Override
        public boolean consumeExact(int count)
        {
            if(hasEnergyInfinityModule)
            {
                var oldEnergy = energy;
                energy = Integer.MAX_VALUE;
                super.consumeExact(count);
                energy = oldEnergy;
                return true;
            }
            else
            {
                return super.consumeExact(count);
            }
        }
    };
    
    private LazyOptional<BetterEnergyStorage> lazyEnergyStorage = LazyOptional.empty();
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    
    public FieldControllerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_CONTROLLER.get(), pWorldPosition, pBlockState);
        uuid = UUID.randomUUID();
        fieldProjectors.add(this);
    }
    
    
    protected IFieldProjector getBestProjectorToBuild(BlockPos blockPos)
    {
        IFieldProjector best = this;
        int minEnergy = -1;
        
        for(var projector : fieldProjectors)
        {
            if(projector.isEnabled() && projector != this)
            {
                int energyToBuild = projector.getEnergyToBuildField(blockPos);
                if(best == null || energyToBuild < minEnergy)
                {
                    best = projector;
                    minEnergy = energyToBuild;
                }
            }
        }
        return best;
    }
    
    protected void distributeField(BlockPos fieldPosition)
    {
        IFieldProjector projector = getBestProjectorToBuild(fieldPosition);
        projector.addField(fieldPosition);
    }
    
    protected void distributeFields(Set<BlockPos> fieldPositions)
    {
        for(var blockPos : fieldPositions)
            distributeField(blockPos);
    }
    
    protected void redistributeFieldsBetween(IFieldProjector from, IFieldProjector to)
    {
        var fields = from.getAllFields();
        for(var blockPos : fields)
        {
            if(from.getEnergyToBuildField(blockPos) > to.getEnergyToBuildField(blockPos))
            {
                from.removeField(blockPos);
                to.addField(blockPos);
            }
        }
    }
    
    protected void removeFieldsFromProjectors(Set<BlockPos> fieldPositions)
    {
        for(var projector : fieldProjectors)
            projector.removeFields(fieldPositions);
    }
    
    protected void removeAllFieldBlocksWhichNotInShapeFromProjectors()
    {
        for(var projector : fieldProjectors)
            projector.removeFieldsIf(blockPos -> shapePositions.contains(blockPos) == false);
    }
    
    protected void updateShape()
    {
        IFieldShape fieldShape = itemStackHandler.getShape();
        if(fieldShape != null)
        {
            var modules = itemStackHandler.getModulesInfo(IFieldModule.class);
            ShapeBuilder shapeBuilder = new ShapeBuilder(this, modules);
            shapePositions = shapeBuilder.init().addFields(fieldShape).build();
        }
        else
        {
            shapePositions = new HashSet<>();
        }
        
        removeAllFieldBlocksWhichNotInShapeFromProjectors();
        HashSet<BlockPos> notDistributedFields = new HashSet<>(shapePositions);
        clearFields();
        for(var projector : fieldProjectors)
            notDistributedFields.removeAll(projector.getAllFields());
        
        distributeFields(notDistributedFields);
    }
    
    protected void updateFieldBlockStatesFromWorldByShape()
    {
        for(var blockPos : shapePositions)
        {
            if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity fieldBlockEntity &&
                    fieldProjectors.contains(fieldBlockEntity.getListener()))
            {
                distributeField(blockPos);
            }
        }
    }
    
    protected void redistributeForNew(IFieldProjector fieldProjector)
    {
        fieldProjector.clearFields();
        for(var otherProjector : fieldProjectors)
        {
            if(otherProjector == fieldProjector)
                continue;
            redistributeFieldsBetween(otherProjector, fieldProjector);
        }
    }
    
    /*protected void updateEnergyStorage()
    {
        var receiveModules = itemStackHandler.getModulesInfo(IEnergyReceiveExtensionModule.class);
        int maxReceive = ENERGY_STORAGE_DEFAULT_RECEIVE;
        for(var module : receiveModules)
        {
            var toAdd = module.getModule().getEnergyReceiveShift() * module.getCount();
            maxReceive += Math.min(Integer.MAX_VALUE - maxReceive, toAdd);
        }
        this.energyStorage.setMaxReceive(maxReceive);
        
        var capacityModules = itemStackHandler.getModulesInfo(IEnergyCapacityExtensionModule.class);
        int capacity = ENERGY_STORAGE_DEFAULT_CAPACITY;
        for(var module : capacityModules)
        {
            var toAdd = module.getModule().getEnergyCapacityShift() * module.getCount();
            capacity += Math.min(Integer.MAX_VALUE - capacity, toAdd);
        }
        this.energyStorage.setMaxEnergyStored(capacity);
        
        hasEnergyInfinityModule = itemStackHandler.getModulesInfo(CreativeEnergyModule.class).isEmpty() == false;
        
        if(hasEnergyInfinityModule)
            this.energyStorage.setEnergyStored(this.energyStorage.getMaxEnergyStored());
    }*/
    
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
    }
    
    @Override
    public void onDisabled()
    {
        super.onDisabled();
        for(var projector : fieldProjectors)
            projector.onControllerDisabled();
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
    protected BetterEnergyStorage getEnergyStorage()
    {
        return energyStorage;
    }
    
    @Override
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    @Override
    public void onDestroyed()
    {
        super.onDestroyed();
        dropInventory();
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
    
    public void onProjectorDisabled(IFieldProjector projector)
    {
        if(projector == this)
            return;
        
        if(fieldProjectors.contains(projector) == false)
            throw new IllegalArgumentException("this controller and projector aren't linked");
        
        var fields = projector.getAllFields();
        projector.clearFields();
        distributeFields(fields);
    }
    
    public void onProjectorEnabled(IFieldProjector projector)
    {
        if(projector == this)
            return;
        
        if(fieldProjectors.contains(projector) == false)
            throw new IllegalArgumentException("this controller and projector aren't linked");
        
        redistributeForNew(projector);
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
            fieldProjectors.clear();
            fieldProjectors.add(this);
            for(var position : WorldLinks.get((ServerLevel)level).getLinks(WorldLinks.getControllerLinkInfo(this)))
            {
                if(level.getBlockEntity(position) instanceof IFieldProjector fieldProjector)
                    fieldProjectors.add(fieldProjector);
            }
            
            //updateEnergyStorage();
            
            updateShape();
            updateFieldBlockStatesFromWorldByShape();
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
                fieldProjectors.add(fieldProjector);
                if(fieldProjector.isEnabled())
                    redistributeForNew(fieldProjector);
            }
        }
    }
    
    @Override
    public void onUnlinked(ServerLevel level, BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof IFieldProjector fieldProjector)
        {
            fieldProjectors.remove(fieldProjector);
            var fields = fieldProjector.getAllFields();
            distributeFields(fields);
            fieldProjector.clearFields();
        }
    }
    
    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos, BlockState blockState)
    {
        super.serverTick(level, blockPos, blockState);
        energyStorage.clearStats();
    }
}
