package me.doggy.energyprotectivefields.item.module.field.shape;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.utils.Math3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class FieldShapeSphereItem extends Item implements IFieldShape
{
    public final int MIN_RADIUS = 4;
    
    public FieldShapeSphereItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    private void buildPart(ShapeBuilder shapeBuilder, int part)
    {
        var centerBlock = shapeBuilder.getCenter();
        var sizes = shapeBuilder.getSizes();
        var strength = shapeBuilder.getStrength();
        
        var dirX = (part & 1) == 0 ? Direction.EAST : Direction.WEST;
        var dirY = (part & 2) == 0 ? Direction.UP : Direction.DOWN;
        var dirZ = (part & 4) == 0 ? Direction.SOUTH : Direction.NORTH;
        
        Vec3i size = new Vec3i(sizes.get(dirX), sizes.get(dirY), sizes.get(dirZ)).offset(MIN_RADIUS, MIN_RADIUS, MIN_RADIUS);
        Vec3i sizeBig = size.offset(strength, strength, strength);
    
        Vec3 centerOfCenterBlockLocal = Vec3.atCenterOf(Vec3i.ZERO);
        
        Vec3i resultMultiplier = new Vec3i(dirX.getNormal().getX(), dirY.getNormal().getY(), dirZ.getNormal().getZ());
    
        for(int x = 0; x <= size.getX() + strength; ++x)
        {
            for(int y = 0; y <= size.getY() + strength; ++y)
            {
                for(int z = 0; z <= size.getZ() + strength; ++z)
                {
                    Vec3i blockPos = new Vec3i(x, y, z);
                    Vec3 closestPoint = Math3D.getClosestPointOfBlock(blockPos, centerOfCenterBlockLocal).subtract(centerOfCenterBlockLocal);
                    Vec3 farthestPoint = Math3D.getFarthestPointOfBlock(blockPos, centerOfCenterBlockLocal).subtract(centerOfCenterBlockLocal);
                
                    double posLocationClosest = (closestPoint.x * closestPoint.x) / (sizeBig.getX() * sizeBig.getX()) +
                                                (closestPoint.y * closestPoint.y)/(sizeBig.getY() * sizeBig.getY()) +
                                                (closestPoint.z * closestPoint.z)/(sizeBig.getZ() * sizeBig.getZ());
                    
                    double posLocationFarthest = (farthestPoint.x * farthestPoint.x)/(size.getX() * size.getX()) +
                                                (farthestPoint.y * farthestPoint.y)/(size.getY() * size.getY()) +
                                                (farthestPoint.z * farthestPoint.z)/(size.getZ() * size.getZ());
                
                    boolean isFarthestOutside = posLocationFarthest > 1;
                    boolean isClosestInside = posLocationClosest <= 1;
                
                    if(isFarthestOutside && isClosestInside)
                    {
                        BlockPos result = Math3D.multiply(blockPos, resultMultiplier).offset(centerBlock);
                        shapeBuilder.addField(result);
                    }
                }
            }
        }
    }
    
    @Override
    public void addFields(ShapeBuilder shapeBuilder)
    {
        for(int i = 0; i < 8; ++i)
            buildPart(shapeBuilder, i);
    }
}
