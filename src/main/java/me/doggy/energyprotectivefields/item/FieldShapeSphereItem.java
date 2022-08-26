package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.api.IFieldShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

public class FieldShapeSphereItem extends Item implements IFieldShape
{
    public final int MIN_RADIUS = 4;
    
    public FieldShapeSphereItem(Properties pProperties)
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
        
        Set<BlockPos> result = new HashSet<>();
    
        int smallRadius = MIN_RADIUS + sizeUpgrade - 1;
        int bigRadius = MIN_RADIUS + sizeUpgrade + strengthUpgrade;
        
        for(int x = 0; x <= bigRadius; ++x)
        {
            for(int y = 0; y <= bigRadius; ++y)
            {
                for(int z = 0; z <= bigRadius; ++z)
                {
                    int dist = (int) Math.round(Math.sqrt(x * x + y * y + z * z));
                    
                    if(dist > smallRadius && dist <= bigRadius)
                    {
                        result.add(center.offset(x, y, z));
                        result.add(center.offset(x, y, -z));
                        result.add(center.offset(x, -y, -z));
                        result.add(center.offset(x, -y, -z));
                        result.add(center.offset(-x, y, z));
                        result.add(center.offset(-x, y, -z));
                        result.add(center.offset(-x, -y, z));
                        result.add(center.offset(-x, -y, -z));
                    }
                }
            }
        }
        
        return result;
    }
}
