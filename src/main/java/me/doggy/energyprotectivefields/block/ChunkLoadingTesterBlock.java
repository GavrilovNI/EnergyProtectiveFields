package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.block.entity.ChunkLoadingTesterBlockEntity;
import me.doggy.energyprotectivefields.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class ChunkLoadingTesterBlock extends BaseEntityBlock
{
    
    protected ChunkLoadingTesterBlock(Properties properties)
    {
        super(properties);
    }
    
    @Override
    public RenderShape getRenderShape(BlockState pState)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new ChunkLoadingTesterBlockEntity(pPos, pState);
    }
    
    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit)
    {
        if(pLevel.isClientSide() == false)
        {
            if(pLevel.getBlockEntity(pPos) instanceof ChunkLoadingTesterBlockEntity blockEntity)
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
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
    {
        if(pLevel.isClientSide())
        {
            return super.getTicker(pLevel, pState, pBlockEntityType);
        }
        else
        {
            return createTickerHelper(pBlockEntityType,  ModBlockEntities.CHUNK_TESTER.get(), ChunkLoadingTesterBlockEntity::tick);
        }
    }
}
