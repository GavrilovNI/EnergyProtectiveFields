package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.api.IStrengthUpgrade;
import net.minecraft.world.item.Item;

public class StrengthUpgradeItem extends Item implements IStrengthUpgrade
{
    private int strengthUpgrade;
    
    public StrengthUpgradeItem(Properties pProperties)
    {
        this(pProperties, 1);
    }
    
    public StrengthUpgradeItem(Properties pProperties, int strengthUpgrade)
    {
        super(pProperties);
        if(strengthUpgrade < 1)
            throw new IllegalArgumentException("strengthUpgrade must be positive");
        this.strengthUpgrade = strengthUpgrade;
    }
    
    @Override
    public int getStrengthMultiplier()
    {
        return strengthUpgrade;
    }
}
