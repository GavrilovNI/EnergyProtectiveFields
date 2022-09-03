package me.doggy.energyprotectivefields.item.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldMover;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;

public class MoveModuleItem extends Item implements IFieldMover
{
    public MoveModuleItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public Vec3i getOffset(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        var direction = moduleInfo.getSlotDirection();
        return direction == null ? Vec3i.ZERO : direction.getNormal();
    }
}
