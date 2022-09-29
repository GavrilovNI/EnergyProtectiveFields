package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.api.utils.MoreNbtUtils;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.networking.NetworkManager;
import me.doggy.energyprotectivefields.screen.ChunkLoadingTesterMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ChunkLoadingTesterBlockEntity extends BlockEntity implements MenuProvider
{
    public static final int DEFAULT_RADIUS = 25;
    
    private int radius = DEFAULT_RADIUS;
    
    private ChunkPos centerChunk = null;
    private Map<BlockPos, BlockState> oldBlockStates = null;
    private BlockState renderingState = ModBlocks.CHUNK_TESTER.get().defaultBlockState();
    
    public ChunkLoadingTesterBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(ModBlockEntities.CHUNK_TESTER.get(), pWorldPosition, pBlockState);
    }
    
    public BlockState getBlockStateByChunkStatus(@Nullable ChunkHolder.FullChunkStatus chunkStatus)
    {
        if(chunkStatus == null)
            return Blocks.BLACK_WOOL.defaultBlockState();
        if(chunkStatus == ChunkHolder.FullChunkStatus.ENTITY_TICKING)
            return Blocks.GREEN_WOOL.defaultBlockState();
        if(chunkStatus == ChunkHolder.FullChunkStatus.TICKING)
            return Blocks.BLUE_WOOL.defaultBlockState();
        if(chunkStatus == ChunkHolder.FullChunkStatus.BORDER)
            return Blocks.YELLOW_WOOL.defaultBlockState();
        if(chunkStatus == ChunkHolder.FullChunkStatus.INACCESSIBLE)
            return Blocks.GRAY_WOOL.defaultBlockState();
        return Blocks.BEDROCK.defaultBlockState();
    }
    
    public int getRadius()
    {
        return radius;
    }
    
    public void setRadius(int radius)
    {
        if(radius < 0)
             throw new IllegalArgumentException("radius must be int rage [0,..)");
        if(level.isClientSide())
        {
            this.radius = radius;
        }
        else
        {
            restoreOldBlockStates();
            this.radius = radius;
            rememberOldBlockStates();
        }
    }
    
    public ChunkPos getCenterChunk()
    {
        return centerChunk;
    }
    
    public void setCenterChunk(ChunkPos chunkPos)
    {
        if(chunkPos.equals(centerChunk) == false)
        {
            centerChunk = chunkPos;
            setChanged();
        }
    }
    
    public BlockState getRenderingState()
    {
        return renderingState;
    }
    
    public void setRenderingState(BlockState blockState)
    {
        if(renderingState.equals(blockState) == false)
        {
            renderingState = blockState;
            if(level instanceof ServerLevel serverLevel)
            {
                var chunkPos = new ChunkPos(worldPosition);
                var chunkMap = serverLevel.getChunkSource().chunkMap;
                var players = chunkMap.getPlayers(chunkPos, false);
                for(var player : players)
                    player.connection.send(getUpdatePacket());
            }
        }
    }
    
    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        CompoundTag nbt = pkt.getTag();
        if (nbt != null) {
            handleUpdateTag(pkt.getTag());
        }
    }
    
    @Override
    public CompoundTag getUpdateTag()
    {
        CompoundTag nbt = super.getUpdateTag();
        nbt.put("rendering_state", NbtUtils.writeBlockState(renderingState));
        return nbt;
    }
    
    @Override
    public void handleUpdateTag(CompoundTag nbt)
    {
        setRenderingState(NbtUtils.readBlockState(nbt.getCompound("rendering_state")));
    }
    
    @Override
    public void setRemoved()
    {
        if(level.isClientSide() == false)
            restoreOldBlockStates();
        super.setRemoved();
    }
    
    @Override
    protected void saveAdditional(CompoundTag pTag)
    {
        if(level.isClientSide() == false)
        {
            pTag.putInt("radius", radius);
            pTag.put("center_chunk", MoreNbtUtils.writeChunkPos(centerChunk));
            if(oldBlockStates != null)
            {
                ListTag blocksInfoTag = new ListTag();
                for(var blockInfo : oldBlockStates.entrySet())
                {
                    CompoundTag blockInfoTag = new CompoundTag();
                    blockInfoTag.put("position", NbtUtils.writeBlockPos(blockInfo.getKey()));
                    blockInfoTag.put("block_state", NbtUtils.writeBlockState(blockInfo.getValue()));
                    blocksInfoTag.add(blockInfoTag);
                }
                pTag.put("blocks_info", blocksInfoTag);
            }
        }
        super.saveAdditional(pTag);
    }
    
    @Override
    public void load(CompoundTag pTag)
    {
        radius = pTag.contains("radius") ? pTag.getInt("radius") : DEFAULT_RADIUS;
    
        if(pTag.contains("center_chunk"))
            centerChunk = MoreNbtUtils.readChunkPos(pTag.getCompound("center_chunk"));
        
        if(pTag.contains("blocks_info"))
        {
            oldBlockStates = new HashMap<>();
            ListTag blocksInfoTag = pTag.getList("blocks_info", Tag.TAG_COMPOUND);
            for(int i = 0; i < blocksInfoTag.size(); i++)
            {
                CompoundTag blockInfoTag = (CompoundTag)blocksInfoTag.get(i);
                BlockPos blockPos = NbtUtils.readBlockPos(blockInfoTag.getCompound("position"));
                BlockState blockState = NbtUtils.readBlockState(blockInfoTag.getCompound("block_state"));
                oldBlockStates.put(blockPos, blockState);
            }
        }
        else
        {
            oldBlockStates = null;
        }
        super.load(pTag);
    }
    
    private void restoreOldBlockStates()
    {
        if(oldBlockStates != null)
        {
            for(var blockInfo : oldBlockStates.entrySet())
                level.setBlock(blockInfo.getKey(), blockInfo.getValue(), 3);
        }
    }
    
    private void rememberOldBlockStates()
    {
        oldBlockStates = new HashMap<>();
        for(int x = -radius; x <= radius; x++)
        {
            for(int z = -radius; z <= radius; z++)
            {
                BlockPos blockPos = worldPosition.offset(x, 0, z);
                oldBlockStates.put(blockPos, level.getBlockState(blockPos));
            }
        }
        oldBlockStates.remove(worldPosition);
        setChanged();
    }
    
    @Override
    public void onLoad()
    {
        super.onLoad();
        
        if(level.isClientSide())
            return;
    
        if(centerChunk == null)
        {
            centerChunk = new ChunkPos(worldPosition);
            setChanged();
        }
        
        if(oldBlockStates == null)
            rememberOldBlockStates();
    }
    
    private void setChunkInfo(int x, int z, BlockState blockState)
    {
        if(x < -radius || x > radius || z < -radius || z > radius)
            throw new IllegalArgumentException("X or Z out of radius.");
        if(x == 0 && z == 0)
        {
            setRenderingState(blockState);
        }
        else
        {
            BlockPos blockPos = worldPosition.offset(x, 0, z);
            level.setBlock(blockPos, blockState, 3);
        }
    }
    
    protected void serverTick()
    {
        for(int x = -radius; x <= radius; x++)
        {
            for(int z = -radius; z <= radius; z++)
            {
                var currentChunkPos = new ChunkPos(centerChunk.x + x, centerChunk.z + z);
                var chunkSource = level.getChunkSource();
    
                var chunk = chunkSource.getChunk(currentChunkPos.x, currentChunkPos.z, false);
                
                if(chunk != null)
                    chunk = chunk;
                
                BlockState blockStateToSet = getBlockStateByChunkStatus(chunk == null ? null : chunk.getFullStatus());
                setChunkInfo(x, z, blockStateToSet);
            }
        }
    }
    
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ChunkLoadingTesterBlockEntity blockEntity)
    {
        if(level.isClientSide() == false)
            blockEntity.serverTick();
    }
    
    @Override
    public Component getDisplayName()
    {
        var id = ModBlocks.CHUNK_TESTER.getId();
        return new TranslatableComponent("block." + id.getNamespace() + "." + id.getPath());
    }
    
    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        return new ChunkLoadingTesterMenu(pContainerId, pInventory, this);
    }
}
