package me.doggy.energyprotectivefields.networking;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.networking.packet.TestEnergyGeneratorSetMaxExtractC2SPacket;
import me.doggy.energyprotectivefields.networking.packet.UpdateEnergyCapabilityInBlockEntityS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager
{
    public static final int CURRENT_MAJOR_VERSION = 1;
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
    
        INSTANCE.messageBuilder(TestEnergyGeneratorSetMaxExtractC2SPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .decoder(TestEnergyGeneratorSetMaxExtractC2SPacket::new)
                .encoder(TestEnergyGeneratorSetMaxExtractC2SPacket::toBytes)
                .consumer(TestEnergyGeneratorSetMaxExtractC2SPacket::handle)
                .add();
    }
}
