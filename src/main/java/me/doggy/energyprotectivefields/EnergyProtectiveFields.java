package me.doggy.energyprotectivefields;

import com.mojang.logging.LogUtils;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.ModBlockEntities;
import me.doggy.energyprotectivefields.data.WorldChunkChanges;
import me.doggy.energyprotectivefields.item.ModItems;
import me.doggy.energyprotectivefields.networking.NetworkManager;
import me.doggy.energyprotectivefields.screen.ModMenuTypes;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(EnergyProtectiveFields.MOD_ID)
public class EnergyProtectiveFields
{
    public static final IEventBus EVENT_BUS = BusBuilder.builder().build();
    
    public static final String MOD_ID = "energyprotectivefields";
    public static final String MOD_NAME = "Energy Protective Fields";
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public EnergyProtectiveFields()
    {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
    
        ModItems.register(eventBus);
        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
    
        eventBus.addListener(this::setupCommon);
        eventBus.addListener(this::setupClient);
        eventBus.addListener(this::setupDedicatedServer);
        
        MinecraftForge.EVENT_BUS.register(this);
        EVENT_BUS.register(WorldChunkChanges.class);
    }
    
    private void setupClient(final FMLClientSetupEvent event)
    {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.FIELD_BLOCK.get(), RenderType.translucent());
    
        ModMenuTypes.registerMenuScreens();
        
        LOGGER.info("Mod " + MOD_NAME + " has started on client!");
    }
    private void setupDedicatedServer(final FMLDedicatedServerSetupEvent event)
    {
        LOGGER.info("Mod " + MOD_NAME + " has started on server!");
    }
    
    private void setupCommon(final FMLCommonSetupEvent event)
    {
        NetworkManager.register();
    }
}
