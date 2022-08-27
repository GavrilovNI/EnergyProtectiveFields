package me.doggy.energyprotectivefields.block;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.ModCreativeModeTab;
import me.doggy.energyprotectivefields.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks
{
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, EnergyProtectiveFields.MOD_ID);
    
    public static final RegistryObject<Block> FIELD_BLOCK = registerBlockWithItem("field_block",
            () -> new FieldBlock(BlockBehaviour.Properties.of(Material.BARRIER)
                    .strength(-1.0F, 3600000.0F)
                    .noDrops()
                    .noOcclusion()
                    .sound(SoundType.GLASS)
                    .isSuffocating(ModBlocks::never)
                    .isValidSpawn(ModBlocks::never)
                    .isViewBlocking(ModBlocks::never)
                    .lightLevel((state) -> 5)
            ),
            ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB
            );
    
    public static final RegistryObject<Block> FIELD_CONTROLLER = registerBlockWithItem("field_controller",
            () -> new FieldControllerBlock(BlockBehaviour.Properties.of(Material.STONE)),
            ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB
    );
    
    public static final RegistryObject<Block> INFINITY_ENERGY_GENERATOR = registerBlockWithItem("infinity_energy_generator",
            () -> new InfinityEnergyGeneratorBlock(BlockBehaviour.Properties.of(Material.STONE)),
            ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB
    );
    
    public static final RegistryObject<Block> FIELD_PROJECTOR = registerBlockWithItem("field_projector",
            () -> new FieldProjector(BlockBehaviour.Properties.of(Material.STONE)),
            ModCreativeModeTab.ENERGY_PROTECTIVE_FIELDS_TAB
    );
    
    private static boolean never(BlockState p_50779_, BlockGetter p_50780_, BlockPos p_50781_, EntityType<?> p_50782_)
    {
        return false;
    }
    private static boolean never(BlockState p_50806_, BlockGetter p_50807_, BlockPos p_50808_)
    {
        return false;
    }
    
    
    private static <T extends Block> RegistryObject<T> registerBlockWithItem(String id, Supplier<T> blockSupplier, CreativeModeTab creativeModeTab)
    {
        RegistryObject<T> block = BLOCKS.register(id, blockSupplier);
        registerBlockItem(id, block, creativeModeTab);
        return block;
    }
    
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String id, RegistryObject<T> block, CreativeModeTab creativeModeTab)
    {
        return ModItems.ITEMS.register(id, () -> new BlockItem(block.get(), new Item.Properties().tab(creativeModeTab)));
    }
    
    public static void register(IEventBus eventBus)
    {
        BLOCKS.register(eventBus);
    }
}
