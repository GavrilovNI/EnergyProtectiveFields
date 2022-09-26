package me.doggy.energyprotectivefields.networking;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.networking.packet.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager
{
    public static final int CURRENT_MAJOR_VERSION = 2;
    public static final int CURRENT_MINOR_VERSION = 0;
    
    private static SimpleChannel INSTANCE;
    
    public static boolean isVersionAccepted(String version)
    {
        var versions = version.split("\\.");
        if(versions.length != 2)
            return false;
    
        try
        {
            int majorVersion = Integer.parseInt(versions[0]);
            return CURRENT_MAJOR_VERSION == majorVersion;
        }
        catch(NumberFormatException exception)
        {
            return false;
        }
    }
    
    @OnlyIn(Dist.CLIENT )
    public static <MSG> void sendToServer(MSG message)
    {
        INSTANCE.sendToServer(message);
    }
    
    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
    
    public static <MSG> void sendToAllPlayers(MSG message, LevelChunk levelChunk)
    {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> levelChunk), message);
    }
    
    public static <MSG> void sendToAllPlayers(MSG message)
    {
        INSTANCE.send(PacketDistributor.ALL.with(() -> null), message);
    }
    
    public static void register()
    {
        INSTANCE = NetworkRegistry.ChannelBuilder.named(new ResourceLocation(EnergyProtectiveFields.MOD_ID, "messages"))
                .networkProtocolVersion(() -> CURRENT_MAJOR_VERSION + "." + CURRENT_MINOR_VERSION)
                .clientAcceptedVersions(NetworkManager::isVersionAccepted)
                .serverAcceptedVersions(NetworkManager::isVersionAccepted)
                .simpleChannel();
        
        
        INSTANCE.messageBuilder(UpdateEnergyCapabilityInBlockEntityS2CPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(UpdateEnergyCapabilityInBlockEntityS2CPacket::new)
                .encoder(UpdateEnergyCapabilityInBlockEntityS2CPacket::serialize)
                .consumer(UpdateEnergyCapabilityInBlockEntityS2CPacket::handle)
                .add();
    
        INSTANCE.messageBuilder(InfinityEnergyGeneratorSetMaxExtractC2SPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .decoder(InfinityEnergyGeneratorSetMaxExtractC2SPacket::new)
                .encoder(InfinityEnergyGeneratorSetMaxExtractC2SPacket::serialize)
                .consumer(InfinityEnergyGeneratorSetMaxExtractC2SPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(ChunkLoadingTesterSetRenderingStateS2CPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ChunkLoadingTesterSetRenderingStateS2CPacket::new)
                .encoder(ChunkLoadingTesterSetRenderingStateS2CPacket::serialize)
                .consumer(ChunkLoadingTesterSetRenderingStateS2CPacket::handle)
                .add();
    
        INSTANCE.messageBuilder(ChunkLoadingTesterSetSettingsS2CPacket.class, 3, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ChunkLoadingTesterSetSettingsS2CPacket::new)
                .encoder(ChunkLoadingTesterSetSettingsS2CPacket::serialize)
                .consumer(ChunkLoadingTesterSetSettingsS2CPacket::handle)
                .add();
        
        INSTANCE.messageBuilder(ChunkLoadingTesterSetSettingsC2SPacket.class, 4, NetworkDirection.PLAY_TO_SERVER)
                .decoder(ChunkLoadingTesterSetSettingsC2SPacket::new)
                .encoder(ChunkLoadingTesterSetSettingsC2SPacket::serialize)
                .consumer(ChunkLoadingTesterSetSettingsC2SPacket::handle)
                .add();
    }
}
