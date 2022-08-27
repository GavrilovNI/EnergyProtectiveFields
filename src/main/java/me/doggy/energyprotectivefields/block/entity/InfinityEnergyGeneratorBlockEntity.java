package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.energy.InfinityEnergyStorage;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.screen.InfinityEnergyGeneratorMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfinityEnergyGeneratorBlockEntity extends BlockEntity implements MenuProvider
{
    private final InfinityEnergyStorage energyStorage = new InfinityEnergyStorage()
    {
        @Override
        public void onChanged()
        {
            super.onChanged();
            setChanged();
        }
    };
    
    private LazyOptional<InfinityEnergyStorage> lazyEnergyStorage;
    
    public InfinityEnergyGeneratorBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.INFINITY_ENERGY_GENERATOR.get(), pWorldPosition, pBlockState);
        lazyEnergyStorage = LazyOptional.of(() -> this.energyStorage);
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
    {
        if(cap == CapabilityEnergy.ENERGY)
            return lazyEnergyStorage.cast();
        
        return super.getCapability(cap, side);
    }
    
    @Override
    public void invalidateCaps()
    {
        super.invalidateCaps();
        lazyEnergyStorage.invalidate();
    }
    
    @Override
    public Component getDisplayName()
    {
        var id = ModBlocks.INFINITY_ENERGY_GENERATOR.getId();
        return new TranslatableComponent("block." + id.getNamespace() + "." + id.getPath());
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        return new InfinityEnergyGeneratorMenu(pContainerId, pInventory, this);
    }
    
    public int getMaxEnergyExtract()
    {
        return energyStorage.getMaxExtract();
    }
    
    public void setMaxEnergyExtract(int maxExtract)
    {
        energyStorage.setMaxExtract(maxExtract);
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
        pTag.put("energyStorage", energyStorage.serializeNBT());
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        if(pTag.contains("energyStorage"))
            energyStorage.deserializeNBT(pTag.getCompound("energyStorage"));
    }
    
    private void outputEnergy()
    {
        for(var direction : Direction.values())
        {
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
            if(blockEntity == null)
                continue;
            
            blockEntity.getCapability(CapabilityEnergy.ENERGY, direction.getOpposite()).ifPresent(storage -> {
                int toSend = this.energyStorage.extractEnergy(energyStorage.getMaxExtract(), true);
                int received = storage.receiveEnergy(toSend, false);
                this.energyStorage.extractEnergy(received, false);
            });
        }
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, InfinityEnergyGeneratorBlockEntity blockEntity)
    {
        if(level.isClientSide() == false)
            blockEntity.outputEnergy();
    }
}
