package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.api.utils.Vec2i;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.ChunkLoadingTesterBlockEntity;
import me.doggy.energyprotectivefields.networking.NetworkManager;
import me.doggy.energyprotectivefields.networking.packet.ChunkLoadingTesterSetSettingsC2SPacket;
import me.doggy.energyprotectivefields.networking.packet.ChunkLoadingTesterSetSettingsS2CPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChunkLoadingTesterMenu extends BaseItemInventoryMenu
{
    private final Player player;
    private final ChunkLoadingTesterBlockEntity blockEntity;
    private final Level level;
    
    private ChunkPos sentChunkPos = null;
    private int sentRadius = -1;
    
    public ChunkLoadingTesterMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData)
    {
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }
    
    public ChunkLoadingTesterMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity)
    {
        super(ModMenuTypes.CHUNK_TESTER_MENU.get(), pContainerId, inventory, new Vec2i(8, 102));
        this.blockEntity = (ChunkLoadingTesterBlockEntity) blockEntity;
        this.level = inventory.player.level;
        this.player = inventory.player;
    }
    
    public ChunkPos getCenterChunk()
    {
        return blockEntity.getCenterChunk();
    }
    
    public void setCenterChunk(ChunkPos chunkPos)
    {
        if(level.isClientSide())
            NetworkManager.sendToServer(new ChunkLoadingTesterSetSettingsC2SPacket(containerId, chunkPos, getRadius()));
        else
            blockEntity.setCenterChunk(chunkPos);
    }
    
    public void resetSettings()
    {
        if(level.isClientSide())
        {
            NetworkManager.sendToServer(new ChunkLoadingTesterSetSettingsC2SPacket(containerId, new ChunkPos(blockEntity.getBlockPos()), ChunkLoadingTesterBlockEntity.DEFAULT_RADIUS));
        }
        else
        {
            blockEntity.setCenterChunk(new ChunkPos(blockEntity.getBlockPos()));
            blockEntity.setRadius(ChunkLoadingTesterBlockEntity.DEFAULT_RADIUS);
        }
    }
    
    public int getRadius()
    {
        return blockEntity.getRadius();
    }
    
    public void setRadius(int radius)
    {
        if(level.isClientSide())
            NetworkManager.sendToServer(new ChunkLoadingTesterSetSettingsC2SPacket(containerId, getCenterChunk(), radius));
        else
            blockEntity.setRadius(radius);
    }
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.CHUNK_TESTER.get());
    }
    
    private boolean isNeededToSend()
    {
        if(sentChunkPos == null)
            return true;
        return blockEntity.getCenterChunk().equals(sentChunkPos) == false || blockEntity.getRadius() != sentRadius;
    }
    
    private void sendEnergyDataToPlayer()
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            var packet = new ChunkLoadingTesterSetSettingsS2CPacket(blockEntity);
            NetworkManager.sendToPlayer(packet, serverPlayer);
            sentChunkPos = packet.centerChunk;
            sentRadius = packet.radius;
        }
    }
    
    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
        
        if(isNeededToSend())
            sendEnergyDataToPlayer();
    }
    
    @Override
    public void sendAllDataToRemote()
    {
        super.sendAllDataToRemote();
        sendEnergyDataToPlayer();
    }
}
