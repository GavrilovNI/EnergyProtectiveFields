 package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class FieldBlock extends BaseEntityBlock
{
    public static final BooleanProperty RENDERING_ITSELF = BooleanProperty.create("rendering_itself");
    
    protected FieldBlock(BlockBehaviour.Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(RENDERING_ITSELF, true));
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        if(pState.getValue(RENDERING_ITSELF))
            return RenderShape.MODEL;
        else
            return RenderShape.INVISIBLE;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder)
    {
        pBuilder.add(RENDERING_ITSELF);
        super.createBlockStateDefinition(pBuilder);
    }
    
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide)
    {
        return pAdjacentBlockState.is(this) ? true : super.skipRendering(pState, pAdjacentBlockState, pSide);
    }
    
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext)
    {
        return Shapes.empty();
    }
    
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos)
    {
        return 1.0F;
    }
    
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos)
    {
        return true;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new FieldBlockEntity(pPos, pState);
    }
}
