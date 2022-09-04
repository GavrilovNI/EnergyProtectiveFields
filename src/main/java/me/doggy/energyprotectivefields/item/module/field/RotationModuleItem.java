package me.doggy.energyprotectivefields.item.module.field;

import me.doggy.energyprotectivefields.api.module.field.IFieldRotator;
import net.minecraft.world.item.Item;

public class RotationModuleItem extends Item implements IFieldRotator
{
    public RotationModuleItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public int getRotation()
    {
        return 1;
    }
}
