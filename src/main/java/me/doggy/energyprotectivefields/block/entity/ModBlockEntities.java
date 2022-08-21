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
    
    public static final RegistryObject<BlockEntityType<FieldControllerBlockEntity>> FIELD_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("field_controller_block_entity", () -> BlockEntityType.Builder.of(FieldControllerBlockEntity::new,
            ModBlocks.FIELD_CONTROLLER.get()).build(null));
    
    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITIES.register(eventBus);
    }
}
