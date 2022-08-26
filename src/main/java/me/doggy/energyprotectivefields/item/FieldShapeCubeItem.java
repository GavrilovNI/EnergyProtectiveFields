package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.api.IFieldShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

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
    public Set<BlockPos> getShieldPoses(BlockPos center, int sizeUpgrade, int strengthUpgrade)
    {
        if(sizeUpgrade < 0)
            throw new IllegalArgumentException("sizeUpgrade must not be negative");
        if(strengthUpgrade < 0)
            throw new IllegalArgumentException("strengthUpgrade must not be negative");
        
        BoundingBox minBounds = getBounds(center, sizeUpgrade, 0);
        BoundingBox maxBounds = getBounds(center, sizeUpgrade, strengthUpgrade);
    
        Set<BlockPos> result = new HashSet<>();
        
        for(int s = 0; s < strengthUpgrade + 1; ++s)
        {
            for(int y = minBounds.minY() + 1; y < minBounds.maxY(); ++y)
            {
                for(int x = maxBounds.minX(); x <= maxBounds.maxX(); ++x)
                {
                    //north
                    result.add(new BlockPos(x, y, minBounds.minZ() - s));
                    //south
                    result.add(new BlockPos(x, y, minBounds.maxZ() + s));
                }
                for(int z = minBounds.minZ() + 1; z < minBounds.maxZ(); ++z)
                {
                    //west
                    result.add(new BlockPos(minBounds.minX() - s, y, z));
                    //east
                    result.add(new BlockPos(minBounds.maxX() + s, y, z));
                }
            }
    
            for(int x = maxBounds.minX(); x <= maxBounds.maxX(); ++x)
            {
                for(int z = maxBounds.minZ(); z <= maxBounds.maxZ(); ++z)
                {
                    //bottom
                    result.add(new BlockPos(x, minBounds.minY() - s, z));
                    //top
                    result.add(new BlockPos(x, minBounds.maxY() + s, z));
                }
            }
        }
        
        return result;
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
