package me.doggy.energyprotectivefields.item.module.field;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldShapeChanger;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

public class DomeModuleItem extends Item implements IFieldShapeChanger
{
    public DomeModuleItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public boolean isInShape(ShapeBuilder shapeBuilder, BlockPos fieldPosition)
    {
        return fieldPosition.getY() >= shapeBuilder.getController().getBlockPos().getY();
    }
}
