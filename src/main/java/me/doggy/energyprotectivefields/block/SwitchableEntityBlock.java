package me.doggy.energyprotectivefields.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public abstract class SwitchableEntityBlock extends BaseEntityBlock
{
    public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
    
    protected SwitchableEntityBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(ENABLED, Boolean.valueOf(false)));
    }
    
    
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(ENABLED, pContext.getLevel().hasNeighborSignal(pContext.getClickedPos()));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.add(ENABLED);
    }
    
    @Override
    public void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving)
    {
        if(pLevel.isClientSide() == false)
        {
            var hasSignal = pLevel.hasNeighborSignal(pPos);
            var wasEnabled = pState.getValue(ENABLED);
            if(wasEnabled != hasSignal)
            {
                pLevel.setBlock(pPos, pState.setValue(ENABLED, hasSignal), 3);
    
                if(hasSignal)
                    onEnabled(pState, pLevel, pPos);
                else
                    onDisabled(pState, pLevel, pPos);
            }
        }
        super.neighborChanged(pState, pLevel, pPos, pBlock, pFromPos, pIsMoving);
    }
    
    protected abstract void onEnabled(BlockState blockState, Level level, BlockPos pos);
    protected abstract void onDisabled(BlockState blockState, Level level, BlockPos pos);
}
