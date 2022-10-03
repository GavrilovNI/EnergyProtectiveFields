package me.doggy.energyprotectivefields.controller;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public interface IFieldBounds
{
    public static final IFieldBounds EMPTY = new IFieldBounds()
    {
        @Nullable
        @Override
        public BoundingBox getBounds()
        {
            return null;
        }
    
        @Override
        public boolean isInsideField(Vec3i blockPos)
        {
            return false;
        }
    };
    
    @Nullable
    BoundingBox getBounds();
    boolean isInsideField(Vec3i blockPos);
}
