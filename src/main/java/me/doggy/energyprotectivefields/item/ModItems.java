package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.ModCreativeModeTab;
import me.doggy.energyprotectivefields.item.module.energy.CreativeEnergyModule;
import me.doggy.energyprotectivefields.item.module.energy.EnergyCapacityExtensionModule;
import me.doggy.energyprotectivefields.item.module.energy.EnergyReceiveExtensionModule;
import me.doggy.energyprotectivefields.item.module.field.*;
import me.doggy.energyprotectivefields.item.module.field.shape.FieldShapeCubeItem;
import me.doggy.energyprotectivefields.item.module.field.shape.FieldShapeCylinderItem;
import me.doggy.energyprotectivefields.item.module.field.shape.FieldShapeSphereItem;
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
    
    public static final RegistryObject<Item> FIELD_SHAPE_CYLINDER = ITEMS.register("field_shape_cylinder", () ->
            new FieldShapeCylinderItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> STRENGTH_UPGRADE = ITEMS.register("strength_upgrade", () ->
            new StrengthUpgradeItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> SIZE_UPGRADE = ITEMS.register("size_upgrade", () ->
            new SizeUpgradeItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> CARD_LINK = ITEMS.register("card_link", () ->
            new LinkingCardItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> DOME_MODULE = ITEMS.register("dome_module", () ->
            new DomeModuleItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> MOVE_MODULE = ITEMS.register("move_module", () ->
            new MoveModuleItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> ENERGY_RECEIVE_UPGRADE = ITEMS.register("energy_receive_upgrade", () ->
            new EnergyReceiveExtensionModule(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB), 20000)
    );
    
    public static final RegistryObject<Item> ENERGY_CAPACITY_UPGRADE = ITEMS.register("energy_capacity_upgrade", () ->
            new EnergyCapacityExtensionModule(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB), 20000)
    );
    
    public static final RegistryObject<Item> CREATIVE_ENERGY_MODULE = ITEMS.register("creative_energy_module", () ->
            new CreativeEnergyModule(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> ROTATION_MODULE = ITEMS.register("rotation_module", () ->
            new RotationModuleItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static final RegistryObject<Item> TUBE_MODULE = ITEMS.register("tube_module", () ->
            new TubeModuleItem(new Item.Properties().tab(ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB))
    );
    
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
