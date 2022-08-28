package me.doggy.energyprotectivefields.item.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IFieldShapeChanger;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
        var size = shapeBuilder.getSize();
        var strength = shapeBuilder.getStrength();
        
        BoundingBox minBounds = getBounds(center, size, 0);
        BoundingBox maxBounds = getBounds(center, size, strength);
    
        HashSet<BlockPos> result = new HashSet<>();
        
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
    
    public BoundingBox getBounds(BlockPos center, int sizeUpgrade, int strengthUpgrade)
    {
        if(sizeUpgrade < 0)
            throw new IllegalArgumentException("sizeUpgrade must not be negative");
        if(strengthUpgrade < 0)
            throw new IllegalArgumentException("strengthUpgrade must not be negative");
        
        int size = DEFAULT_RADIUS + sizeUpgrade + strengthUpgrade;
        
        BlockPos minPos = center.offset(-size, -size, -size);
        BlockPos maxPos = center.offset(size, size, size);
        
        return BoundingBox.fromCorners(minPos, maxPos);
    }
}
