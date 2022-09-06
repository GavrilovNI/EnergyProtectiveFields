package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;

public interface IFieldShapeValidator extends IFieldModule
{
    boolean isInShape(ShapeBuilder shapeBuilder, Vec3i fieldPosition);
    
    @Override
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo){}
}
