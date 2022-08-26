package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES,
            EnergyProtectiveFields.MOD_ID);
    
    
    public static final RegistryObject<BlockEntityType<FieldBlockEntity>> FIELD_BLOCK =
            BLOCK_ENTITIES.register("field_block", () -> BlockEntityType.Builder.of(FieldBlockEntity::new,
                    ModBlocks.FIELD_BLOCK.get()).build(null));
    
    public static final RegistryObject<BlockEntityType<FieldControllerBlockEntity>> FIELD_CONTROLLER =
            BLOCK_ENTITIES.register("field_controller", () -> BlockEntityType.Builder.of(FieldControllerBlockEntity::new,
            ModBlocks.FIELD_CONTROLLER.get()).build(null));

    public static final RegistryObject<BlockEntityType<InfinityEnergyGeneratorBlockEntity>> INFINITY_ENERGY_GENERATOR =
            BLOCK_ENTITIES.register("infinity_energy_generator", () -> BlockEntityType.Builder.of(InfinityEnergyGeneratorBlockEntity::new,
                    ModBlocks.INFINITY_ENERGY_GENERATOR.get()).build(null));
    
    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITIES.register(eventBus);
    }
}
