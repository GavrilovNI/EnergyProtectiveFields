package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldBlockEntity extends BlockEntity
{
    private boolean lostProjector = false;
    private BlockPos projectorPosition = null;
    
    private BlockState camouflage = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
    
    public FieldBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_BLOCK.get(), pWorldPosition, pBlockState);
    }
    
    public void setProjectorPosition(@Nullable BlockPos blockPos)
    {
        var oldProjector = getProjectorIfLoaded();
        projectorPosition = blockPos;
        var newProjector = getProjectorIfLoaded();
        if(oldProjector != newProjector)
        {
            if(oldProjector != null)
                oldProjector.onFieldDestroyed(this);
            if(newProjector != null)
                newProjector.onFieldCreated(this);
        }
    }
    
    public BlockState getCamouflage()
    {
        return camouflage;
    }
    
    public void setCamouflage(BlockState camouflage)
    {
        if(camouflage.isAir())
            camouflage = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
        
        if(this.camouflage.equals(camouflage) == false)
        {
            this.camouflage = camouflage;
    
            boolean renderingItself = camouflage.equals(ModBlocks.FIELD_BLOCK.get().defaultBlockState());
            var blockState = level.getBlockState(worldPosition);
            if(blockState.hasProperty(FieldBlock.RENDERING_ITSELF) && blockState.getValue(FieldBlock.RENDERING_ITSELF) != renderingItself)
                level.setBlock(worldPosition, blockState.setValue(FieldBlock.RENDERING_ITSELF, renderingItself), 2+8+16+32);
            
            setChanged();
        }
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        CompoundTag nbt = pkt.getTag();
        if (nbt != null) {
            handleUpdateTag(pkt.getTag());
        }
    }
    
    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("camouflage", NbtUtils.writeBlockState(camouflage));
        return nbt;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag nbt)
    {
        setCamouflage(NbtUtils.readBlockState(nbt.getCompound("camouflage")));
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        return super.serializeNBT();
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        super.deserializeNBT(nbt);
    }
    
    @Override
    public CompoundTag getTileData()
    {
        return super.getTileData();
    }
    
    public boolean hasProjector()
    {
        return projectorPosition != null;
    }
    
    public <T extends IFieldProjector> boolean isMyProjector(T projector)
    {
        if(projectorPosition == null)
            return false;
        
        return projector instanceof IFieldProjector &&
                projector.getLevel().equals(getLevel()) &&
                projector.getPosition().equals(projectorPosition);
    }
    
    @Nullable
    public IFieldProjector getProjectorIfLoaded()
    {
        if(projectorPosition == null)
            return null;
        if(level.isLoaded(projectorPosition))
        {
            if(level.getBlockEntity(projectorPosition) instanceof IFieldProjector projector)
                return projector;
            else
                onLostProjector(false);
        }
        return null;
    }
    
    @Nullable
    public BlockPos getProjectorPosition()
    {
        return projectorPosition;
    }
    
    protected void onLostProjector(boolean destroy)
    {
        projectorPosition = null;
        EnergyProtectiveFields.LOGGER.warn("FieldBlockEntity(" + worldPosition.toShortString() + ") lost it's projector and was not removed." + (destroy ? " Destroying..." : "Keeping..."));
        if(destroy)
            level.removeBlock(worldPosition, false);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        var projector = getProjectorIfLoaded();
        if(projector != null)
            projector.onFieldCreated(this);
        else if(lostProjector)
            onLostProjector(true);
        setCamouflage(camouflage);
    }
    
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        var projector = getProjectorIfLoaded();
        if(projector != null)
            projector.onFieldDestroyed(this);
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
    
        if(projectorPosition != null)
            pTag.put("projector_pos", NbtUtils.writeBlockPos(projectorPosition));
        if(getBlockState().getValue(FieldBlock.RENDERING_ITSELF) == false)
            pTag.put("camouflage", NbtUtils.writeBlockState(camouflage));
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        
        if(pTag.contains("projector_pos"))
        {
            projectorPosition = NbtUtils.readBlockPos(pTag.getCompound("projector_pos"));
        }
        else
        {
            projectorPosition = null;
            lostProjector = true;
        }
        
        if(pTag.contains("camouflage"))
            camouflage = NbtUtils.readBlockState(pTag.getCompound("camouflage"));
        else
            camouflage = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
        
    }
    
}
