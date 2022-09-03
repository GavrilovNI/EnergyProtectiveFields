package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.IDestroyingHandler;
import me.doggy.energyprotectivefields.IServerTickable;
import me.doggy.energyprotectivefields.api.ISwitchingHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFieldProjectorBlock extends SwitchableEntityBlock
{
    protected AbstractFieldProjectorBlock(Properties properties)
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
                if(pLevel.getBlockEntity(pPos) instanceof IDestroyingHandler destroyingHandler)
                    destroyingHandler.onDestroyed();
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
    
    @Override
    protected void onEnabled(BlockState blockState, Level level, BlockPos pos)
    {
        if(level.isClientSide() == false && level.getBlockEntity(pos) instanceof ISwitchingHandler switchingHandler)
            switchingHandler.onEnabled();
    }
    
    @Override
    protected void onDisabled(BlockState blockState, Level level, BlockPos pos)
    {
        if(level.isClientSide() == false && level.getBlockEntity(pos) instanceof ISwitchingHandler switchingHandler)
            switchingHandler.onDisabled();
    }
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if(pLevel.isClientSide() == false)
        {
            if(pLevel.getBlockEntity(pPos) instanceof MenuProvider blockEntity)
                NetworkHooks.openGui((ServerPlayer)pPlayer, blockEntity, pPos);
            else
                throw new IllegalStateException("Menu provider is missing!");
        }
        
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }
    
    public abstract <T extends BlockEntity & ISwitchingHandler & IDestroyingHandler & IServerTickable> T createBlockEntity(BlockPos pPos, BlockState pState);
    public abstract <T extends  BlockEntity & ISwitchingHandler & IDestroyingHandler & IServerTickable> BlockEntityType<T> getRegisteredBlockEntity();
    
    @Nullable
    @Override
    public final BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return createBlockEntity(pPos, pState);
    }
    
    private static <T extends BlockEntity> void tick(Level pLevel, BlockPos pPos, BlockState pState, T pBlockEntity)
    {
        ((IServerTickable)pBlockEntity).serverTick((ServerLevel)pLevel, pPos, pState);
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        if(pLevel.isClientSide())
        {
            return super.getTicker(pLevel, pState, pBlockEntityType);
        }
        else
        {
            return createTickerHelper(pBlockEntityType, getRegisteredBlockEntity(), AbstractFieldProjectorBlock::tick);
        }
    }
}
