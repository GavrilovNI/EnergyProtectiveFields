package me.doggy.energyprotectivefields.command;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EnergyProtectiveFields.MOD_ID)
public class ModCommands
{
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event)
    {
        var dispatcher = event.getDispatcher();
        
        FindControllersTestCommand.register(dispatcher);
        GetControllerBoundsCommand.register(dispatcher);
    }
}
