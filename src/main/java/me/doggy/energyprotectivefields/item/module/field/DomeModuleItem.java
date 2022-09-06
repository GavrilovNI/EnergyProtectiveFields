package me.doggy.energyprotectivefields.item.module.field;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldShapeValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;

public class DomeModuleItem extends Item implements IFieldShapeValidator
{
    public DomeModuleItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public boolean isInShape(ShapeBuilder shapeBuilder, Vec3i fieldPosition)
    {
        return fieldPosition.getY() >= shapeBuilder.getController().getBlockPos().getY();
    }
}
