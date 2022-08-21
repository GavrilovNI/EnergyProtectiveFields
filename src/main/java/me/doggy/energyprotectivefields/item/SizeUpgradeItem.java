package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.block.ISizeUpgrade;
import net.minecraft.world.item.Item;

public class SizeUpgradeItem extends Item implements ISizeUpgrade
{
    private int upgradeStrength;
    
    public SizeUpgradeItem(Properties pProperties)
    {
        this(pProperties, 1);
    }
    
    public SizeUpgradeItem(Properties pProperties, int upgradeStrength)
    {
        super(pProperties);
        if(upgradeStrength < 1)
            throw new IllegalArgumentException("upgradeStrength must be positive");
        this.upgradeStrength = upgradeStrength;
    }
    
    @Override
    public int getUpgradeStrength()
    {
        return upgradeStrength;
    }
}
