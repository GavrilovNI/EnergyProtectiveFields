package me.doggy.energyprotectivefields.item.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IFieldShapeChanger;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import java.util.Collection;
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
    public void addFields(ShapeBuilder shapeBuilder)
    {
        var center = shapeBuilder.getCenter();
        var size = shapeBuilder.getSize();
        var strength = shapeBuilder.getStrength();
        
        HashSet<BlockPos> result = new HashSet<>();
    
        int smallRadius = MIN_RADIUS + size - 1;
        int bigRadius = MIN_RADIUS + size + strength;
        
        for(int x = 0; x <= bigRadius; ++x)
        {
            for(int y = 0; y <= bigRadius; ++y)
            {
                for(int z = 0; z <= bigRadius; ++z)
                {
                    int dist = (int) Math.round(Math.sqrt(x * x + y * y + z * z));
                    
                    if(dist > smallRadius && dist <= bigRadius)
                    {
                        shapeBuilder.addField(center.offset(x, y, z));
                        shapeBuilder.addField(center.offset(x, y, -z));
                        shapeBuilder.addField(center.offset(x, -y, z));
                        shapeBuilder.addField(center.offset(x, -y, -z));
                        shapeBuilder.addField(center.offset(-x, y, z));
                        shapeBuilder.addField(center.offset(-x, y, -z));
                        shapeBuilder.addField(center.offset(-x, -y, z));
                        shapeBuilder.addField(center.offset(-x, -y, -z));
                    }
                }
            }
        }
    }
}
