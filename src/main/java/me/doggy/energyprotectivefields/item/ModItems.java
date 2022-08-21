package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems
{
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, EnergyProtectiveFields.MOD_ID);
    
    public static final RegistryObject<Item> FIELD_SHAPE_CUBE = ITEMS.register("field_shape_cube", () ->
            new FieldShapeCubeItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> FIELD_SHAPE_SPHERE = ITEMS.register("field_shape_sphere", () ->
            new FieldShapeSphereItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> SIZE_UPGRADE = ITEMS.register("size_upgrade", () ->
            new SizeUpgradeItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
