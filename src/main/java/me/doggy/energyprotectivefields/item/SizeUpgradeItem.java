package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.api.ISizeUpgrade;
import net.minecraft.world.item.Item;

public class SizeUpgradeItem extends Item implements ISizeUpgrade
{
    private int sizeUpgrade;
    
    public SizeUpgradeItem(Properties pProperties)
    {
        this(pProperties, 1);
    }
    
    public SizeUpgradeItem(Properties pProperties, int sizeUpgrade)
    {
        super(pProperties);
        if(sizeUpgrade < 1)
            throw new IllegalArgumentException("sizeUpgrade must be positive");
        this.sizeUpgrade = sizeUpgrade;
    }
    
    @Override
    public int getSizeMultiplier()
    {
        return sizeUpgrade;
    }
}
