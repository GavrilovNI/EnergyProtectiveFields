package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes
{
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, EnergyProtectiveFields.MOD_ID);
    
    public static final RegistryObject<MenuType<FieldControllerMenu>> FIELD_CONTROLLER_MENU = registerMenuType(FieldControllerMenu::new,
            "field_controller_menu");
    
    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> containerFactory, String id)
    {
        return MENU_TYPES.register(id, () -> IForgeMenuType.create(containerFactory));
    }
    
    public static void register(IEventBus eventBus)
    {
        MENU_TYPES.register(eventBus);
    }
}
