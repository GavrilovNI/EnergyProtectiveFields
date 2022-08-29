package me.doggy.energyprotectivefields.item.module.shape;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;

public class FieldShapeCylinderItem extends Item implements IFieldShape
{
    public final int DEFAULT_RADIUS = 4;
    public final int MIN_HEIGHT = 1;
    
    public FieldShapeCylinderItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    private interface Predicate4<A,B,C,D>
    {
        boolean test(A a, B b, C c, D d);
    }
    
    //info: https://enchantia.com/software/graphapp/doc/tech/ellipses.html#:~:text=3.1%20McIlroy%27s%20Ellipse%20Algorithm%201
    private void drawCylinderQuarter(ShapeBuilder shapeBuilder, int quarterIndex, int addRadius)
    {
        int radiusX = DEFAULT_RADIUS + addRadius;
        int radiusZ = DEFAULT_RADIUS + addRadius;
        
        var sizes = shapeBuilder.getSizes();
    
        var offsetDown = sizes.get(Direction.DOWN) - ((MIN_HEIGHT - 1) / 2);
        var height = offsetDown + sizes.get(Direction.UP) + MIN_HEIGHT;
    
        var centerBlock = shapeBuilder.getCenter().offset(0, -offsetDown, 0);
    
        int offsetMultiplierX;
        int offsetMultiplierZ;
        
        switch(quarterIndex)
        {
            case 0:
                radiusX += sizes.get(Direction.EAST);
                radiusZ += + sizes.get(Direction.SOUTH);
                offsetMultiplierX = 1;
                offsetMultiplierZ = 1;
                break;
            case 1:
                radiusX += + sizes.get(Direction.WEST);
                radiusZ += + sizes.get(Direction.SOUTH);
                offsetMultiplierX = -1;
                offsetMultiplierZ = 1;
                break;
            case 2:
                radiusX += + sizes.get(Direction.WEST);
                radiusZ += + sizes.get(Direction.NORTH);
                offsetMultiplierX = -1;
                offsetMultiplierZ = -1;
                break;
            case 3:
                radiusX += + sizes.get(Direction.EAST);
                radiusZ += + sizes.get(Direction.NORTH);
                offsetMultiplierX = 1;
                offsetMultiplierZ = -1;
                break;
            default:
                throw new IllegalArgumentException("quarterIndex out of bounds [0,3]");
        }
    
        long radiusXsqr = ((long)radiusX) * radiusX;
        long radiusZsqr = ((long)radiusZ) * radiusZ;
    
        int x = 0;
        int z = radiusZ;
        long crit1 = -(radiusXsqr / 4 + radiusX % 2 + radiusZsqr);
        long crit2 = -(radiusZsqr / 4 + radiusZ % 2 + radiusXsqr);
        long crit3 = -(radiusZsqr / 4 + radiusZ % 2);
        long t = -radiusXsqr * z;
        long dxt = 0; // x * 2 * radiusZsqr;
        long dzt = -2 * radiusXsqr * z;
        long d2xt = 2 * radiusZsqr;
        long d2zt = 2 * radiusXsqr;
        
        while(z >= 0 && x <= radiusX)
        {
            for(int y = 0; y < height; y++)
                shapeBuilder.addField(centerBlock.offset(x * offsetMultiplierX, y, z * offsetMultiplierZ));
            
            if(t + radiusZsqr * x <= crit1 || t + radiusXsqr * z <= crit3)
            {
                x++;
                dxt += d2xt;
                t += dxt;
            }
            else if(t - radiusXsqr * z > crit2)
            {
                z--;
                dzt += d2zt;
                t += dzt;
            }
            else
            {
                x++;
                dxt += d2xt;
                t += dxt;
                z--;
                dzt += d2zt;
                t += dzt;
            }
        }
        
    }
    
    @Override
    public void addFields(ShapeBuilder shapeBuilder)
    {
        for(int s = 0; s <= shapeBuilder.getStrength(); ++s)
            for(int i = 0; i < 4; ++i)
                drawCylinderQuarter(shapeBuilder, i, DEFAULT_RADIUS + s);
    }
}
