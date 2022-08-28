package me.doggy.energyprotectivefields.item.module;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.ISizeUpgrade;
import net.minecraft.world.item.Item;

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
        var sizes = shapeBuilder.getSizes();
        var strength = shapeBuilder.getStrength();
        
        var totalSize = 0;
        for(var size : sizes.values())
        {
            if(totalSize < size)
                totalSize = size;
        }
        totalSize += MIN_RADIUS;
    
        int smallRadius = totalSize - 1;
        int bigRadius = totalSize + strength;
    
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
