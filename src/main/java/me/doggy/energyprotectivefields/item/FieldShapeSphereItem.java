package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.IFieldShape;
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
    public Set<BlockPos> getShieldPoses(BlockPos center, int sizeUpgrade)
    {
        Set<BlockPos> result = new HashSet<>();
    
        int radius = MIN_RADIUS + sizeUpgrade;
        
        for(int x = 0; x <= radius; ++x)
        {
            for(int y = 0; y <= radius; ++y)
            {
                for(int z = 0; z <= radius; ++z)
                {
                    int dist = (int) Math.round(Math.sqrt(x * x + y * y + z * z));
                    
                    if(dist > radius - 1 && dist <= radius)
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
