package me.doggy.energyprotectivefields.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FieldBlockEntity extends BlockEntity
{
    private BlockPos controllerPosition = null;
    
    public FieldBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.FIELD_BLOCK.get(), pWorldPosition, pBlockState);
    }
    
    public void setControllerPosition(BlockPos position)
    {
        var oldController = getController();
        controllerPosition = position;
        var newController = getController();
        if(oldController != newController)
        {
            if(oldController != null)
                oldController.onFieldBlockDestroyed(getBlockPos());
            if(newController != null)
                newController.onFieldBlockCreated(getBlockPos());
        }
    }
    
    public boolean isMyController(FieldControllerBlockEntity fieldControllerBlockEntity)
    {
        if(controllerPosition == null)
            return false;
        return fieldControllerBlockEntity.getLevel().equals(getLevel()) &&
                fieldControllerBlockEntity.getBlockPos().equals(controllerPosition);
    }
    
    @Nullable
    public BlockPos getControllerPosition()
    {
        return controllerPosition;
    }
    
    @Nullable
    public FieldControllerBlockEntity getController()
    {
        if(controllerPosition == null)
            return null;
        if(level.hasChunk(SectionPos.blockToSectionCoord(controllerPosition.getX()), SectionPos.blockToSectionCoord(controllerPosition.getZ())))
        {
            var blockEntity = level.getBlockEntity(controllerPosition);
            if(blockEntity instanceof FieldControllerBlockEntity fieldControllerBlockEntity)
                return fieldControllerBlockEntity;
            else
                onLostController();
        }
        return null;
    }
    
    private void onLostController()
    {
        controllerPosition = null;
        level.removeBlock(getBlockPos(), false);
    }
    
    @Nullable
    @Override
    public void onLoad()
    {
        super.onLoad();
        var controller = getController();
        if(controller != null)
            controller.onFieldBlockCreated(getBlockPos());
    }
    
    @Override
    public void setRemoved()
    {
        var controller = getController();
        if(controller != null)
            controller.onFieldBlockDestroyed(getBlockPos());
        super.setRemoved();
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
        
        if(controllerPosition != null)
            pTag.put("controller-pos", NbtUtils.writeBlockPos(controllerPosition));
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        
        if(pTag.contains("controller-pos"))
            controllerPosition = NbtUtils.readBlockPos(pTag.getCompound("controller-pos"));
        else
            controllerPosition = null;
    }
    
}
