package me.doggy.energyprotectivefields;

import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.block.ModBlocks;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab
{
    public static final CreativeModeTab ENERGY_PROTECTIVE_FIELDS_TAB = new CreativeModeTab(EnergyProtectiveFields.MOD_ID + "tab")
    {
        @Override
        public ItemStack makeIcon()
        {
            return new ItemStack(ModBlocks.FIELD_BLOCK.get());
        }
    };
}
