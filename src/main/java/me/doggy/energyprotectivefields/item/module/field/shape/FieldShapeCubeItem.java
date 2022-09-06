package me.doggy.energyprotectivefields.item.module.field.shape;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.module.field.ITubeModule;
import me.doggy.energyprotectivefields.item.module.field.TubeModuleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Map;

public class FieldShapeCubeItem extends Item implements IFieldShape
{
    public final int DEFAULT_RADIUS = 4;
    
    public FieldShapeCubeItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public void addFields(ShapeBuilder shapeBuilder)
    {
        var center = shapeBuilder.getCenter();
        var sizes = shapeBuilder.getSizes();
        var strength = shapeBuilder.getStrength();
        
        BoundingBox minBounds = getBounds(center, sizes, 0);
        BoundingBox maxBounds = getBounds(center, sizes, strength);
        
        boolean hasNotTubeModule = shapeBuilder.hasModule(ITubeModule.class) == false;
        
        for(int s = 0; s < strength + 1; ++s)
        {
            for(int y = minBounds.minY() + 1; y < minBounds.maxY(); ++y)
            {
                for(int x = maxBounds.minX(); x <= maxBounds.maxX(); ++x)
                {
                    //north
                    shapeBuilder.addField(new BlockPos(x, y, minBounds.minZ() - s));
                    //south
                    shapeBuilder.addField(new BlockPos(x, y, minBounds.maxZ() + s));
                }
                for(int z = minBounds.minZ() + 1; z < minBounds.maxZ(); ++z)
                {
                    //west
                    shapeBuilder.addField(new BlockPos(minBounds.minX() - s, y, z));
                    //east
                    shapeBuilder.addField(new BlockPos(minBounds.maxX() + s, y, z));
                }
            }
    
            if(hasNotTubeModule)
            {
                for(int x = maxBounds.minX(); x <= maxBounds.maxX(); ++x)
                {
                    for(int z = maxBounds.minZ(); z <= maxBounds.maxZ(); ++z)
                    {
                        //bottom
                        shapeBuilder.addField(new BlockPos(x, minBounds.minY() - s, z));
                        //top
                        shapeBuilder.addField(new BlockPos(x, minBounds.maxY() + s, z));
                    }
                }
            }
        }
    }
    
    @Override
    public boolean isInside(ShapeBuilder shapeBuilder, Vec3i blockPos)
    {
        var center = shapeBuilder.getCenter();
        var sizes = shapeBuilder.getSizes();
        var strength = shapeBuilder.getStrength();
    
        BoundingBox maxBounds = getBounds(center, sizes, strength);
        
        return maxBounds.isInside(blockPos);
    }
    
    public BoundingBox getBounds(BlockPos center, Map<Direction, Integer> sizes, int strengthUpgrade)
    {
        if(strengthUpgrade < 0)
            throw new IllegalArgumentException("strengthUpgrade must not be negative");
        
        int minSize = DEFAULT_RADIUS + strengthUpgrade;
        
        BlockPos minPos = center.offset(-minSize - sizes.get(Direction.WEST), -minSize - sizes.get(Direction.DOWN), -minSize - sizes.get(Direction.NORTH));
        BlockPos maxPos = center.offset(minSize + sizes.get(Direction.EAST), minSize + sizes.get(Direction.UP), minSize + sizes.get(Direction.SOUTH));
        
        return BoundingBox.fromCorners(minPos, maxPos);
    }
}
