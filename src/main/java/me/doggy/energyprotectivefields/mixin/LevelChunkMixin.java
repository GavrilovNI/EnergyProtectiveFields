package me.doggy.energyprotectivefields.mixin;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.event.LevelChunkEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunk.class)
public class LevelChunkMixin
{
    @Inject(at = @At(value = "RETURN"),
            method = "setBlockState")
    private void EnergyProtectiveFields_onBlockStateChanged(BlockPos blockPos, BlockState pState, boolean pIsMoving, CallbackInfoReturnable<BlockState> callbackInfo)
    {
        LevelChunk levelChunk = (LevelChunk)(Object)this;
        BlockState oldState = callbackInfo.getReturnValue();
        BlockState newState = pState;
        if(oldState != null)
            EnergyProtectiveFields.EVENT_BUS.post(new LevelChunkEvent.BlockStateChanged(levelChunk, blockPos, oldState, newState));
    }

}
