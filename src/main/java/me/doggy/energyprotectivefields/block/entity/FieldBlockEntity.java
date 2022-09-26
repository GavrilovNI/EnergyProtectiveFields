package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.jetbrains.annotations.Nullable;

public class FieldBlockEntity extends BlockEntity
{
    private boolean lostProjector = false;
    private BlockPos projectorPosition = null;
    
    public FieldBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_BLOCK.get(), pWorldPosition, pBlockState);
    }
    
    public void setProjectorPosition(@Nullable BlockPos blockPos)
    {
        var oldProjector = getProjector();
        projectorPosition = blockPos;
        var newProjector = getProjector();
        if(oldProjector != newProjector)
        {
            if(oldProjector != null)
                oldProjector.onFieldDestroyed(this);
            if(newProjector != null)
                newProjector.onFieldCreated(this);
        }
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
    public IFieldProjector getProjector()
    {
        if(projectorPosition == null)
            return null;
        if(level.getBlockEntity(projectorPosition) instanceof IFieldProjector projector)
            return projector;
        else
            onLostProjector();
        return null;
    }
    
    protected void onLostProjector()
    {
        projectorPosition = null;
        EnergyProtectiveFields.LOGGER.warn("FieldBlockEntity(" + worldPosition.toShortString() + ") lost it's projector and was not removed.");
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        var projector = getProjector();
        if(projector != null)
            projector.onFieldCreated(this);
        else if(lostProjector)
            onLostProjector();
    }
    
    @Override
    public void setRemoved()
    {
        super.setRemoved();
        var projector = getProjector();
        if(projector != null)
            projector.onFieldDestroyed(this);
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
    
        if(projectorPosition != null)
            pTag.put("projectorPos", NbtUtils.writeBlockPos(projectorPosition));
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        
        if(pTag.contains("projectorPos"))
        {
            projectorPosition = NbtUtils.readBlockPos(pTag.getCompound("projector-pos"));
        }
        else
        {
            projectorPosition = null;
            lostProjector = true;
        }
    }
    
}
