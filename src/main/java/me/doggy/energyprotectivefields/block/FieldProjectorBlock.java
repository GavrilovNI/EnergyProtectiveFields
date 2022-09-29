package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.IServerTickable;
import me.doggy.energyprotectivefields.api.ISwitchingHandler;
import me.doggy.energyprotectivefields.block.entity.FieldProjectorBlockEntity;
import me.doggy.energyprotectivefields.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class FieldProjectorBlock extends AbstractFieldProjectorBlock
{
    protected FieldProjectorBlock(Properties properties)
    {
        super(properties);
    }
    
    @Override
    public <T extends BlockEntity & ISwitchingHandler & IServerTickable> T createBlockEntity(BlockPos pPos, BlockState pState)
    {
        return (T)new FieldProjectorBlockEntity(pPos, pState);
    }
    
    @Override
    public <T extends BlockEntity & ISwitchingHandler & IServerTickable> BlockEntityType<T> getRegisteredBlockEntity()
    {
        return (BlockEntityType<T>)ModBlockEntities.FIELD_PROJECTOR.get();
    }
}
