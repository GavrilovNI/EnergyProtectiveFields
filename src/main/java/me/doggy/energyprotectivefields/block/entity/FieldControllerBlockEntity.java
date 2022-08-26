package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.IFieldShape;
import me.doggy.energyprotectivefields.api.IStrengthUpgrade;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.energy.InfinityEnergyStorage;
import me.doggy.energyprotectivefields.block.FieldControllerBlock;
import me.doggy.energyprotectivefields.api.ISizeUpgrade;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.screen.FieldControllerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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

import java.util.*;

public class FieldControllerBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int SLOT_FIELD_SHAPE  = 0;
    public static final int SLOT_SIZE_UPGRADE  = 1;
    public static final int SLOT_STRENGTH_UPGRADE  = 2;
    
    public static final int ITEM_CAPABILITY_SIZE = 3;
    
    public static final int MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK = 100;
    public static final int MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK = 1000;
    
    public static final int MAX_ENERGY_CAPACITY = 50000;
    public static final int MAX_ENERGY_RECEIVE= 2000;
    
    public static final int ENERGY_TO_BUILD = 20;
    public static final int ENERGY_TO_SUPPORT = 4;
    
    private UUID uuid;
    
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
                        case SLOT_FIELD_SHAPE -> IFieldShape.class;
                        case SLOT_SIZE_UPGRADE -> ISizeUpgrade.class;
                        case SLOT_STRENGTH_UPGRADE -> IStrengthUpgrade.class;
                        default -> null;
                    };
            
            if(classNeeded == null)
                return false;
            
            return getAs(itemStack, classNeeded) != null;
        }
    
        @Override
        public int getSlotLimit(int slot)
        {
            if(slot == SLOT_FIELD_SHAPE)
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
    
    private Set<BlockPos> shapePositions = new HashSet<>();
    
    private HashSet<BlockPos> createdFields = new HashSet<>();
    private HashSet<BlockPos> notCreatedFields = new HashSet<>();
    
    private HashSet<BlockPos> fieldsToRemove = new HashSet<>();
    
    public FieldControllerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_CONTROLLER.get(), pWorldPosition, pBlockState);
        uuid = UUID.randomUUID();
    }
    
    private static <T> T getAs(ItemStack itemStack, Class<T> clazz)
    {
        ItemLike itemLike = itemStack.getItem();
        if(clazz.isAssignableFrom(itemLike.getClass()))
            return (T)itemLike;
        if(itemLike instanceof BlockItem blockItem)
        {
            var block = blockItem.getBlock();
            if(clazz.isAssignableFrom(block.getClass()))
                return (T)block;
        }
        return null;
    }
    
    public UUID getUuid()
    {
        return uuid;
    }
    
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    private void onInventoryContentChanged(int slot)
    {
        updateShape();
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
        
        createdFields.remove(position);
        if(shapePositions.contains(position))
            notCreatedFields.add(position);
        
        fieldsToRemove.remove(position);
    }
    
    public void onFieldBlockCreated(BlockPos position)
    {
        if(level.isClientSide())
            return;
        
        createdFields.add(position);
        notCreatedFields.remove(position);
    }
    
    private void updateShape()
    {
        IFieldShape fieldShape = getAs(itemStackHandler.getStackInSlot(SLOT_FIELD_SHAPE), IFieldShape.class);
        if(fieldShape != null)
        {
            var sizeUpgradeStack = itemStackHandler.getStackInSlot(SLOT_SIZE_UPGRADE);
            int sizeUpgrade = sizeUpgradeStack.isEmpty() ? 0 : sizeUpgradeStack.getCount() * getAs(sizeUpgradeStack, ISizeUpgrade.class).getSizeMultiplier();
    
            var strengthUpgradeStack = itemStackHandler.getStackInSlot(SLOT_STRENGTH_UPGRADE);
            int strengthUpgrade = strengthUpgradeStack.isEmpty() ? 0 : strengthUpgradeStack.getCount() * getAs(strengthUpgradeStack, IStrengthUpgrade.class).getStrengthMultiplier();
    
            shapePositions = fieldShape.getShieldPoses(worldPosition, sizeUpgrade, strengthUpgrade);
        }
        else
        {
            shapePositions = new HashSet<>();
        }
    
        fieldsToRemove.removeAll(shapePositions);
        requestToRemoveAllFieldBlocksWhichNotInShape();
        updateFieldBlockStatesFromWorldByShape();
    }
    
    private boolean canBuildIn(BlockPos position)
    {
        return level.isEmptyBlock(position);
    }
    
    private void initFieldBlock(BlockPos position)
    {
        FieldBlockEntity fieldBlockEntity = (FieldBlockEntity)level.getBlockEntity(position);
        fieldBlockEntity.setControllerPosition(worldPosition);
    }
    
    @Deprecated // you should not remove all field blocks instantly
    private void removeAllCreatedFieldBlocksInstantly()
    {
        requestToRemoveAllCreatedFieldBlocks();
        removeRequestedFields(fieldsToRemove.size());
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
            }
            else
            {
                notCreatedFields.add(position);
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
    }
    
    
    private void removeRequestedFields(int count)
    {
        for(var position : fieldsToRemove.stream().limit(count).toList())
            level.removeBlock(position, false);
    }
    
    private void createFieldBlocks(int count)
    {
        for(var position : notCreatedFields.stream().toList())
        {
            if(count <= 0)
                break;
            
            if(canBuildIn(position))
            {
                if(energyStorage.consumeExact(getEnergyToBuild(position)))
                {
                    boolean created = level.setBlock(position, ModBlocks.FIELD_BLOCK.get().defaultBlockState(), 3);
                    if(created)
                    {
                        initFieldBlock(position);
                        count--;
                    }
                    else
                    {
                        energyStorage.receiveEnergy(ENERGY_TO_BUILD, false);
                    }
                }
            }
        }
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
    
    private int getEnergyToSupport(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr < 25)
        {
            return ENERGY_TO_SUPPORT;
        }
        else
        {
            return (int)(ENERGY_TO_SUPPORT * distanceToBlockSqr / 25);
        }
    }
    
    private int getEnergyToBuild(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr < 25)
        {
            return ENERGY_TO_BUILD;
        }
        else
        {
            return (int)(ENERGY_TO_BUILD * distanceToBlockSqr / 25);
        }
    }
    
    private void consumeEnergyForCreatedFields()
    {
        for(var position : createdFields)
        {
            if(energyStorage.consumeExact(getEnergyToSupport(position)))
                continue;
            
            fieldsToRemove.add(position);
        }
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FieldControllerBlockEntity blockEntity)
    {
        if(level.isClientSide())
            return;
    
        blockEntity.removeRequestedFields(MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK);
        blockEntity.consumeEnergyForCreatedFields();
        
        if(blockEntity.isEnabled())
            blockEntity.createFieldBlocks(MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK);
    }
    
}
