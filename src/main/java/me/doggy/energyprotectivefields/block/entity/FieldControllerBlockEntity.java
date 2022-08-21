package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.IFieldShape;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.FieldControllerBlock;
import me.doggy.energyprotectivefields.block.ISizeUpgrade;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class FieldControllerBlockEntity extends BlockEntity implements MenuProvider
{
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(2)
    {
        @Override
        protected void onContentsChanged(int slot)
        {
            setChanged();
            onInventoryContentChanged(slot);
        }
    
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack)
        {
            if(slot == 0)
                return stack.isEmpty() || stack.getItem() instanceof IFieldShape;
            if(slot == 1)
                return stack.isEmpty() || stack.getItem() instanceof ISizeUpgrade;
            return true;
        }
    };
    
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();
    
    public FieldControllerBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_CONTROLLER_BLOCK_ENTITY.get(), pWorldPosition, pBlockState);
    }
    
    private Set<BlockPos> allShieldPositions = null;
    private Set<BlockPos> createdFields = new HashSet<>();
    
    public boolean isEnabled()
    {
        return getBlockState().getValue(FieldControllerBlock.ENABLED);
    }
    
    private void onInventoryContentChanged(int slot)
    {
        updateShape();
        removeFieldBlocksWhichNotInShape();
    }
    
    public void onBlockRemoved()
    {
        if(level.isClientSide())
            return;
        
        removeField();
    }
    
    private void updateShape()
    {
        if(level.isClientSide())
            return;
        
        
        int sizeUpgrade = 0;
        
        var sizeUpgradeStack = itemStackHandler.getStackInSlot(1);
        if(sizeUpgradeStack.isEmpty() == false)
            sizeUpgrade = sizeUpgradeStack.getCount() * ((ISizeUpgrade)(sizeUpgradeStack.getItem())).getUpgradeStrength();
        
        if(itemStackHandler.getStackInSlot(0).getItem() instanceof IFieldShape fieldShape)
            allShieldPositions = fieldShape.getShieldPoses(getBlockPos(), sizeUpgrade);
        else
            allShieldPositions = new HashSet<>();
    }
    
    private boolean tryCreateFieldBlock(BlockPos position)
    {
        if(level.isEmptyBlock(position))
        {
            boolean created = level.setBlock(position, ModBlocks.FIELD_BLOCK.get().defaultBlockState(), 3);
            if(created)
                createdFields.add(position);
            return created;
        }
        else if(level.getBlockState(position).getBlock() instanceof FieldBlock)
        {
            createdFields.add(position);
            return true;
        }
        
        return false;
    }
    
    private void updateCreatedFieldBlocksFromWorld()
    {
        createdFields.clear();
        for(var position : allShieldPositions)
        {
            if(level.getBlockState(position).getBlock() instanceof FieldBlock)
            {
                createdFields.add(position);
            }
        }
    }
    
    private void removeFieldBlocksWhichNotInShape()
    {
        if(level.isClientSide())
            return;
        
        for(var position : createdFields.stream().toList())
        {
            if(allShieldPositions.contains(position))
                continue;
            if(level.getBlockState(position).getBlock() instanceof FieldBlock)
            {
                boolean removed = level.removeBlock(position, false);
                if(removed)
                    createdFields.remove(position);
            }
        }
    }
    
 
 
    private void removeField()
    {
        if(level.isClientSide())
            return;
        
        for(var position : createdFields.stream().toList())
        {
            if(level.getBlockState(position).getBlock() instanceof FieldBlock)
            {
                boolean removed = level.removeBlock(position, false);
                if(removed)
                    createdFields.remove(position);
            }
        }
    }
    
    private void buildField()
    {
        if(level.isClientSide())
            return;
        
        for(var fieldBlockPosition : allShieldPositions)
            tryCreateFieldBlock(fieldBlockPosition);
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
        {
            return lazyItemHandler.cast();
        }
        
        return super.getCapability(cap, side);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemStackHandler);
    
        if(level.isClientSide() == false)
        {
            updateShape();
            updateCreatedFieldBlocksFromWorld();
        }
    }
    
    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        pTag.put("inventory", itemStackHandler.serializeNBT());
        super.saveAdditional(pTag);
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        itemStackHandler.deserializeNBT(pTag.getCompound("inventory"));
        super.load(pTag);
    }
    
    public void dropInventory()
    {
        SimpleContainer inventory = new SimpleContainer(itemStackHandler.getSlots());
        for(int i = 0; i < itemStackHandler.getSlots(); i++)
        {
            inventory.setItem(i, itemStackHandler.getStackInSlot(i));
        }
    
        Containers.dropContents(level, worldPosition, inventory);
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FieldControllerBlockEntity blockEntity)
    {
        if(level.isClientSide())
            return;
        if(blockEntity.isEnabled())
        {
            if(blockEntity.allShieldPositions != null)
                blockEntity.buildField();
        }
        else
        {
            blockEntity.removeField();
        }
    }
    
}
