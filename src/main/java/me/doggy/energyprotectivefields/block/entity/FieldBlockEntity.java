package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.IFieldStateListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldBlockEntity extends BlockEntity
{
    private BlockPos projectorPosition = null;
    
    public FieldBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_BLOCK.get(), pWorldPosition, pBlockState);
    }
    
    public void setProjectorPosition(@Nullable BlockPos blockPos)
    {
        var oldProjector = getProjectorIfChunkIsLoaded();
        projectorPosition = blockPos;
        var newProjector = getProjectorIfChunkIsLoaded();
        if(oldProjector != newProjector)
        {
            if(oldProjector != null)
                oldProjector.onFieldDestroyed(this);
            if(newProjector != null)
                newProjector.onFieldCreated(this);
        }
    }
    
    
    
    public <T extends BlockEntity> boolean isMyProjector(T projector)
    {
        if(projectorPosition == null)
            return false;
        return projector instanceof IFieldProjector &&
                projector.getLevel().equals(getLevel()) &&
                projector.getBlockPos().equals(projectorPosition);
    }
    
    @Nullable
    public IFieldProjector getProjectorIfChunkIsLoaded()
    {
        if(projectorPosition == null)
            return null;
        if(level.hasChunk(SectionPos.blockToSectionCoord(projectorPosition.getX()), SectionPos.blockToSectionCoord(projectorPosition.getZ())))
        {
            var blockEntity = level.getBlockEntity(projectorPosition);
            if(isMyProjector(blockEntity))
                return (IFieldProjector)blockEntity;
            else
                onLostProjector();
        }
        return null;
    }
    
    @Nullable
    public IFieldStateListener getListener()
    {
        if(projectorPosition == null)
            return null;
        if(level.getBlockEntity(projectorPosition) instanceof IFieldStateListener projector)
            return projector;
        else
            onLostProjector();
        return null;
    }
    
    protected void onLostProjector()
    {
        projectorPosition = null;
        level.removeBlock(getBlockPos(), false);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        var projector = getListener();
        if(projector != null)
            projector.onFieldCreated(this);
    }
    
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        var projector = getListener();
        if(projector != null)
            projector.onFieldDestroyed(this);
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
        
        if(projectorPosition != null)
            pTag.put("projector-pos", NbtUtils.writeBlockPos(projectorPosition));
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        
        if(pTag.contains("projector-pos"))
            projectorPosition = NbtUtils.readBlockPos(pTag.getCompound("projector-pos"));
        else
            projectorPosition = null;
    }
    
}
