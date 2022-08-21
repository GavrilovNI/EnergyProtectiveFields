package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.IFieldShape;
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
    public Set<BlockPos> getShieldPoses(BlockPos center, int sizeUpgrade)
    {
        BoundingBox bounds = getBounds(center, sizeUpgrade);
    
        Set<BlockPos> result = new HashSet<>();
    
        for(int y = bounds.minY(); y <= bounds.maxY(); ++y)
        {
            for(int x = bounds.minX(); x <= bounds.maxX(); ++x)
            {
                //north
                result.add(new BlockPos(x, y, bounds.minZ()));
                //south
                result.add(new BlockPos(x, y, bounds.maxZ()));
            }
            for(int z = bounds.minZ() + 1; z < bounds.maxZ(); ++z)
            {
                //west
                result.add(new BlockPos(bounds.minX(), y, z));
                //east
                result.add(new BlockPos(bounds.maxX(), y, z));
            }
        }
        
        for(int x = bounds.minX() + 1; x < bounds.maxX(); ++x)
        {
            for(int z = bounds.minZ() + 1; z < bounds.maxZ(); ++z)
            {
                //bottom
                result.add(new BlockPos(x, bounds.minY(), z));
                //top
                result.add(new BlockPos(x, bounds.maxY(), z));
            }
        }
        
        return result;
    }
    
    public BoundingBox getBounds(BlockPos center, int sizeUpgrade)
    {
        int size = DEFAULT_RADIUS + sizeUpgrade;
        
        BlockPos minPos = center.offset(-size, -size, -size);
        BlockPos maxPos = center.offset(size, size, size);
        
        return BoundingBox.fromCorners(minPos, maxPos);
    }
}
