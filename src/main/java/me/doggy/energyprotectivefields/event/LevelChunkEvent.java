package me.doggy.energyprotectivefields.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.eventbus.api.Event;

public abstract class LevelChunkEvent extends Event
{
    public final LevelChunk levelChunk;
    
    public LevelChunkEvent(LevelChunk levelChunk)
    {
        this.levelChunk = levelChunk;
    }
    
    public static class BlockStateChanged extends LevelChunkEvent
    {
        public final BlockPos blockPos;
        public final BlockState oldState;
        public final BlockState newState;
    
        public BlockStateChanged(LevelChunk levelChunk, BlockPos blockPos, BlockState oldState, BlockState newState)
        {
            super(levelChunk);
            this.blockPos = blockPos;
            this.oldState = oldState;
            this.newState = newState;
        }
    }
}
