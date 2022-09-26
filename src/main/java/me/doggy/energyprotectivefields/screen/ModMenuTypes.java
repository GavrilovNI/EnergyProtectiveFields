package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
    
    public static final RegistryObject<MenuType<InfinityEnergyGeneratorMenu>> INFINITY_ENERGY_GENERATOR_MENU = registerMenuType(
            InfinityEnergyGeneratorMenu::new,
            "infinity_energy_generator_menu");
    
    public static final RegistryObject<MenuType<FieldProjectorMenu>> FIELD_PROJECTOR_MENU = registerMenuType(FieldProjectorMenu::new,
            "field_projector_menu");
    
    public static final RegistryObject<MenuType<ChunkLoadingTesterMenu>> CHUNK_TESTER_MENU = registerMenuType(ChunkLoadingTesterMenu::new,
            "chunk_loading_tester_menu");
    
    private static <T extends AbstractContainerMenu>RegistryObject<MenuType<T>> registerMenuType(IContainerFactory<T> containerFactory, String id)
    {
        return MENU_TYPES.register(id, () -> IForgeMenuType.create(containerFactory));
    }
    
    public static void register(IEventBus eventBus)
    {
        MENU_TYPES.register(eventBus);
    }
    
    @OnlyIn(Dist.CLIENT)
    public static void registerMenuScreens()
    {
        MenuScreens.register(ModMenuTypes.FIELD_CONTROLLER_MENU.get(), FieldControllerScreen::new);
        MenuScreens.register(ModMenuTypes.INFINITY_ENERGY_GENERATOR_MENU.get(), InfinityEnergyGeneratorScreen::new);
        MenuScreens.register(ModMenuTypes.FIELD_PROJECTOR_MENU.get(), FieldProjectorScreen::new);
        MenuScreens.register(ModMenuTypes.CHUNK_TESTER_MENU.get(), ChunkLoadingTesterScreen::new);
    }
}
