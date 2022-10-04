package me.doggy.energyprotectivefields.api.module.field;

import me.doggy.energyprotectivefields.api.CancellationToken;
import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public interface IFieldShape extends IFieldModule
{
    void addFields(ShapeBuilder shapeBuilder, CancellationToken cancellationToken);
    
    @Override
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo){}
    
    boolean isInside(ShapeBuilder shapeBuilder, Vec3i blockPos);
}
