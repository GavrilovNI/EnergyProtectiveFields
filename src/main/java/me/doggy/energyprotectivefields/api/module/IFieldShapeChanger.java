package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public interface IFieldShapeChanger extends IShapeModule
{
    boolean isInShape(ShapeBuilder shapeBuilder, BlockPos fieldPosition);
    
    @Override
    default int getLimitInControllerSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo){}
}
