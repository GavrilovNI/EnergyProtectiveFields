package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Collection;
import java.util.Set;

public interface IFieldShape extends IModule
{
    void addFields(ShapeBuilder shapeBuilder);
    
    @Override
    default int getLimitInControllerSlot(ItemStack itemStack)
    {
        return 1;
    }
    
    @Override
    default void applyOnInit(ShapeBuilder builder, int stackSize){}
}
