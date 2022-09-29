package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.IServerTickable;
import me.doggy.energyprotectivefields.api.ISwitchingHandler;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FieldControllerBlock extends AbstractFieldProjectorBlock
{
    protected FieldControllerBlock(Properties properties)
    {
        super(properties);
    }
    
    @Override
    public <T extends BlockEntity & ISwitchingHandler & IServerTickable> T createBlockEntity(BlockPos pPos, BlockState pState)
    {
         return (T)new FieldControllerBlockEntity(pPos, pState);
    }
    
    @Override
    public <T extends BlockEntity & ISwitchingHandler & IServerTickable> BlockEntityType<T> getRegisteredBlockEntity()
    {
        return (BlockEntityType<T>)ModBlockEntities.FIELD_CONTROLLER.get();
    }
}
