package me.doggy.energyprotectivefields.api.module;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.ShapeBuilder;
import net.minecraft.core.Direction;

public interface ISizeUpgrade extends IDirectionalShapeModule
{
    int getSizeMultiplier();
    
    @Override
    default void applyOnInit(ShapeBuilder builder, ModuleInfo moduleInfo)
    {
        var additionalSize = getSizeMultiplier() * moduleInfo.getCount();
        var direction = moduleInfo.getSlotDirection();
        if(direction == null)
        {
            for(var dir : Direction.values())
                builder.setSize(dir, builder.getSize(dir) + additionalSize);
        }
        else
        {
            builder.setSize(direction, builder.getSize(direction) + additionalSize);
        }
    }
}
