package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.block.entity.FieldProjectorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class FieldProjector extends SwitchableEntityBlock
{
    protected FieldProjector(Properties properties)
    {
        super(properties);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.MODEL;
    }
    
    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving)
    {
        if(pLevel.isClientSide() == false)
        {
            if(pState.getBlock() != pNewState.getBlock())
            {
                if(pLevel.getBlockEntity(pPos) instanceof FieldProjectorBlockEntity blockEntity)
                {
                    blockEntity.dropInventory();
                    blockEntity.onDestroyed();
                }
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
    
    @Override
    protected void onEnabled(BlockState blockState, Level level, BlockPos pos)
    {
    
    }
    
    @Override
    protected void onDisabled(BlockState blockState, Level level, BlockPos pos)
    {
    
    }
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if(pLevel.isClientSide() == false)
        {
            if(pLevel.getBlockEntity(pPos) instanceof FieldProjectorBlockEntity blockEntity)
            {
                NetworkHooks.openGui((ServerPlayer)pPlayer, blockEntity, pPos);
            }
            else
            {
                throw new IllegalStateException("Container provider is missing!");
            }
        }
        
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new FieldProjectorBlockEntity(pPos, pState);
    }
}
