package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.*;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.module.*;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FieldControllerBlockEntity extends BlockEntity implements MenuProvider, IFieldProjector, IHaveUUID, ILinkable
{
    
    
    public static final int MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK = 100;
    public static final int MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK = 1000;
    
    public static final int MAX_ENERGY_CAPACITY = 50000;
    public static final int MAX_ENERGY_RECEIVE = 10000;
    
    public static final int ENERGY_TO_BUILD = 0;
    public static final int ENERGY_TO_SUPPORT = 0;
    
    private UUID uuid;
    private final Random random;
    
    private final FieldControllerItemStackHandler itemStackHandler = new FieldControllerItemStackHandler()
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            if(level.isClientSide() == false)
                updateShape();
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
    
    private final Set<IFieldProjector> fieldProjectors = new HashSet<>();
    
    private Set<BlockPos> shapePositions = new HashSet<>();
    private final HashSet<BlockPos> createdFields = new HashSet<>();
    private final HashSet<BlockPos> fieldsToRemove = new HashSet<>();
    
    private HashSet<BlockPos> notCreatedFields = new HashSet<>();
    private HashSet<BlockPos> notCreatedTwiceFields = new HashSet<>();
    
    public FieldControllerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_CONTROLLER.get(), pWorldPosition, pBlockState);
        uuid = UUID.randomUUID();
        fieldProjectors.add(this);
        random = new Random();
    }
    
    @Override
    public UUID getUUID()
    {
        return uuid;
    }
    
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    public void onDestroyed()
    {
        if(level.isClientSide())
            return;
        
        removeAllCreatedFieldBlocksInstantly(); // TODO: should not remove instantly
    }
    
    public void onFieldBlockDestroyed(BlockPos position)
    {
        if(level.isClientSide())
            return;
    
        if(level.getBlockEntity(position) instanceof FieldBlockEntity fieldBlockEntity
                && fieldBlockEntity.isMyController(this))
            return;
        
        createdFields.remove(position);
        if(shapePositions.contains(position))
            notCreatedFields.add(position);
        
        fieldsToRemove.remove(position);
    }
    
    public void onFieldBlockCreated(BlockPos position)
    {
        if(level.isClientSide())
            return;
        
        if(level.getBlockEntity(position) instanceof FieldBlockEntity fieldBlockEntity && fieldBlockEntity.isMyController(this))
        {
            createdFields.add(position);
            notCreatedFields.remove(position);
            notCreatedTwiceFields.remove(position);
    
            if(shapePositions.contains(position) == false)
                fieldsToRemove.add(position);
        }
    }
    
    private void updateShape()
    {
        IFieldShape fieldShape = itemStackHandler.getShape();
        if(fieldShape != null)
        {
            var modules = itemStackHandler.getModulesInfo(IShapeModule.class);
            ShapeBuilder shapeBuilder = new ShapeBuilder(this, modules);
            shapePositions = shapeBuilder.init().addFields(fieldShape).build();
        }
        else
        {
            shapePositions = new HashSet<>();
        }
    
        fieldsToRemove.removeAll(shapePositions);
        requestToRemoveAllFieldBlocksWhichNotInShape();
        
        notCreatedFields.addAll(shapePositions);
        notCreatedFields.removeAll(createdFields);
        notCreatedFields.removeAll(notCreatedTwiceFields);
        //updateFieldBlockStatesFromWorldByShape();
    }
    
    private boolean canBuildIn(BlockPos position)
    {
        return level.isEmptyBlock(position);
    }
    
    private boolean isImpossibleToBuildIn(BlockPos position)
    {
        return level.isOutsideBuildHeight(position);
    }
    
    private void initFieldBlock(BlockPos position)
    {
        FieldBlockEntity fieldBlockEntity = (FieldBlockEntity)level.getBlockEntity(position);
        fieldBlockEntity.setControllerPosition(worldPosition);
    }
    
    private void updateFieldBlockStatesFromWorldByShape()
    {
        for(var position : shapePositions)
        {
            if(level.getBlockEntity(position) instanceof FieldBlockEntity fieldBlockEntity &&
                    fieldBlockEntity.isMyController(this))
            {
                createdFields.add(position);
                notCreatedFields.remove(position);
                notCreatedTwiceFields.remove(position);
            }
            else
            {
                notCreatedFields.add(position);
                notCreatedTwiceFields.remove(position);
                createdFields.remove(position);
            }
        }
    }
    
    private void requestToRemoveAllCreatedFieldBlocks()
    {
        fieldsToRemove.addAll(createdFields);
    }
    
    private void requestToRemoveAllFieldBlocksWhichNotInShape()
    {
        for(var position : createdFields)
        {
            if(shapePositions.contains(position) == false)
                fieldsToRemove.add(position);
        }
        for(var position : notCreatedFields.stream().toList())
        {
            if(shapePositions.contains(position) == false)
                notCreatedFields.remove(position);
        }
    
        for(var position : notCreatedTwiceFields.stream().toList())
        {
            if(shapePositions.contains(position) == false)
                notCreatedTwiceFields.remove(position);
        }
    }
    
    @Deprecated // you should not remove all field blocks instantly
    private void removeAllCreatedFieldBlocksInstantly()
    {
        requestToRemoveAllCreatedFieldBlocks();
        removeRequestedFields(fieldsToRemove.size());
    }
    
    private void createFieldBlocks(int count)
    {
        if(notCreatedFields.isEmpty() && notCreatedTwiceFields.isEmpty() == false)
        {
            notCreatedFields = notCreatedTwiceFields;
            notCreatedTwiceFields = new HashSet<>();
        }
        Set<BlockPos> justCreatedFields = new HashSet<>();
        Set<BlockPos> fieldsToEraseFromSet = new HashSet<>();
        
        for(var position : notCreatedFields)
        {
            if(count <= 0)
                break;
            if(isImpossibleToBuildIn(position))
            {
                fieldsToEraseFromSet.add(position);
                continue;
            }
            else
            {
                boolean created = false;
                if(canBuildIn(position))
                {
                    IFieldProjector fieldProjector = getBestProjectorToBuild(position);
                    if(fieldProjector != null)
                    {
                        fieldProjector.onBuiltEnergyField(position);
                
                        created = level.setBlock(position, ModBlocks.FIELD_BLOCK.get().defaultBlockState(), 3);
                        if(created)
                        {
                            justCreatedFields.add(position);
                            count--;
                        }
                    }
                }
                if(created == false)
                {
                    fieldsToEraseFromSet.add(position);
                    notCreatedTwiceFields.add(position);
                }
            }
        }
        
        notCreatedFields.removeAll(fieldsToEraseFromSet);
        for(var position : justCreatedFields)
            initFieldBlock(position);
    }
    
    private void removeRequestedFields(int count)
    {
        for(var position : fieldsToRemove.stream().limit(count).toList())
            level.removeBlock(position, false);
    }
    
    @Nullable
    private IFieldProjector getBestProjectorToBuild(BlockPos blockPos)
    {
        IFieldProjector best = null;
        int minEnergy = -1;
        
        for(var projector : fieldProjectors)
        {
            if(projector.canBuildEnergyField(blockPos))
            {
                int energyToBuild = projector.getEnergyToBuildEnergyField(blockPos);
                if(best == null || energyToBuild < minEnergy)
                {
                    best = projector;
                    minEnergy = energyToBuild;
                }
            }
        }
        return best;
    }
    
    @Nullable
    private IFieldProjector getBestProjectorToSupport(BlockPos blockPos)
    {
        IFieldProjector best = null;
        int minEnergy = -1;
        
        for(var projector : fieldProjectors)
        {
            if(projector.canSupportEnergyField(blockPos))
            {
                int energyToSupport = projector.getEnergyToSupportEnergyField(blockPos);
                if(best == null || energyToSupport < minEnergy)
                {
                    best = projector;
                    minEnergy = energyToSupport;
                }
            }
        }
        return best;
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
    
        if(level.isClientSide() == false)
        {
            fieldProjectors.clear();
            fieldProjectors.add(this);
            for(var position : WorldLinks.get((ServerLevel)level).getLinks(WorldLinks.getControllerLinkInfo(this)))
            {
                if(level.getBlockEntity(position) instanceof IFieldProjector fieldProjector)
                    fieldProjectors.add(fieldProjector);
            }
            
            updateShape();
            updateFieldBlockStatesFromWorldByShape();
        }
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
        pTag.putUUID("uuid", uuid);
        pTag.put("inventory", itemStackHandler.serializeNBT());
        pTag.put("energy", energyStorage.serializeNBT());
        super.saveAdditional(pTag);
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        uuid = pTag.getUUID("uuid");
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
    
    public void onDisabled()
    {
        if(level.isClientSide() == false)
            requestToRemoveAllCreatedFieldBlocks();
    }
    
    public void onEnabled()
    {
    
    }
    
    private void consumeEnergyForSupportingFields()
    {
        for(var position : createdFields)
        {
            IFieldProjector fieldProjector = getBestProjectorToSupport(position);
            if(fieldProjector == null)
            {
                fieldsToRemove.add(position);
                continue;
            }
            fieldProjector.onSupportedEnergyField(position);
        }
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FieldControllerBlockEntity blockEntity)
    {
        if(level.isClientSide())
            return;
    
        blockEntity.removeRequestedFields(MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK);
        blockEntity.consumeEnergyForSupportingFields();
        
        if(blockEntity.isEnabled())
            blockEntity.createFieldBlocks(MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK);
    }
    
    @Override
    public int getEnergyToBuildEnergyField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 25)
        {
            return ENERGY_TO_BUILD;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(ENERGY_TO_BUILD * (distance - 4));
        }
    }
    
    @Override
    public int getEnergyToSupportEnergyField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 25)
        {
            return ENERGY_TO_SUPPORT;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(ENERGY_TO_SUPPORT * (distance - 4));
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
    
    @Override
    public void onLinked(ServerLevel level, BlockPos blockPos)
    {
        if(this.level == level)
        {
            if(level.getBlockEntity(blockPos) instanceof IFieldProjector fieldProjector)
                fieldProjectors.add(fieldProjector);
        }
    }
    
    @Override
    public void onUnlinked(ServerLevel level, BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof IFieldProjector fieldProjector)
            fieldProjectors.remove(fieldProjector);
    }
}
