package me.doggy.energyprotectivefields.item.module.field.shape;

import me.doggy.energyprotectivefields.api.ShapeBuilder;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.module.field.ITubeModule;
import me.doggy.energyprotectivefields.api.utils.Math3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public class FieldShapeCylinderItem extends Item implements IFieldShape
{
    public final int MIN_RADIUS = 4;
    public final int MIN_HEIGHT = 4;
    
    public FieldShapeCylinderItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    private void buildCylinderQuarter2(ShapeBuilder shapeBuilder, int quarterIndex)
    {
        var sizes = shapeBuilder.getSizes();
        var additiveStrength = shapeBuilder.getStrength();
    
        var dirX = (quarterIndex & 1) == 0 ? Direction.EAST : Direction.WEST;
        var dirZ = (quarterIndex & 2) == 0 ? Direction.SOUTH : Direction.NORTH;
    
        var sizeDown = sizes.get(Direction.DOWN);
        var offsetInnerDown = sizeDown + ((MIN_HEIGHT - 1) / 2) - 1;
        var innerHeight = Math.max(sizeDown + sizes.get(Direction.UP) + MIN_HEIGHT - 2, 0);
    
        Vec3i size = new Vec3i(sizes.get(dirX), innerHeight, sizes.get(dirZ)).offset(MIN_RADIUS, MIN_RADIUS, MIN_RADIUS);
        Vec3i sizeBig = size.offset(additiveStrength, additiveStrength, additiveStrength);
    
        Vec3 centerOfCenterBlockLocal = Vec3.atCenterOf(Vec3i.ZERO).subtract(0, offsetInnerDown, 0);
        Vec3i resultMultiplier = new Vec3i(dirX.getNormal().getX(), 1, dirZ.getNormal().getZ());
        
        boolean hasTubeModule = shapeBuilder.hasModule(ITubeModule.class);
        
        for(int x = 0; x <= size.getX() + additiveStrength; ++x)
        {
            for(int z = 0; z <= size.getZ() + additiveStrength; ++z)
            {
                Vec3i blockPos = new Vec3i(x, -offsetInnerDown, z);
                
                Vec3 closestPoint = Math3D.getClosestPointOfBlock(blockPos, centerOfCenterBlockLocal).subtract(centerOfCenterBlockLocal);
                double posLocationClosest = (closestPoint.x * closestPoint.x) / (sizeBig.getX() * sizeBig.getX()) +
                                            (closestPoint.z * closestPoint.z) / (sizeBig.getZ() * sizeBig.getZ());
                boolean isClosestInside = posLocationClosest <= 1;
            
                if(isClosestInside)
                {
                    BlockPos result = Math3D.multiplyByAxes(blockPos, resultMultiplier);
    
                    Vec3 farthestPoint = Math3D.getFarthestPointOfBlock(blockPos, centerOfCenterBlockLocal).subtract(centerOfCenterBlockLocal);
                    double posLocationFarthest = (farthestPoint.x * farthestPoint.x) / (size.getX() * size.getX()) +
                            (farthestPoint.z * farthestPoint.z) / (size.getZ() * size.getZ());
                    boolean isFarthestOutside = posLocationFarthest > 1;
                    
                    if(innerHeight > 0 && isFarthestOutside)
                    {
                        for(int y = 0; y < innerHeight; y++)
                            shapeBuilder.addFieldByVector(result.above(y));
                    }
                    
                    if(isFarthestOutside || hasTubeModule == false)
                    {
                        for(int i = 0; i <= additiveStrength; ++i)
                        {
                            shapeBuilder.addFieldByVector(result.below(i + 1));
                            shapeBuilder.addFieldByVector(result.above(i + innerHeight));
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public void addFields(ShapeBuilder shapeBuilder)
    {
        for(int i = 0; i < 4; ++i)
            buildCylinderQuarter2(shapeBuilder, i);
    }
    
    protected boolean isInsideInfinityCylinder(ShapeBuilder shapeBuilder, Vec3i blockPos, int quarterIndex)
    {
        var centerBlock = shapeBuilder.getCenter();
        var sizes = shapeBuilder.getSizes();
        var additiveStrength = shapeBuilder.getStrength();
        
        var sizeDown = sizes.get(Direction.DOWN);
        var offsetInnerDown = sizeDown + ((MIN_HEIGHT - 1) / 2) - 1;
        var innerHeight = Math.max(sizeDown + sizes.get(Direction.UP) + MIN_HEIGHT - 2, 0);
    
        var dirX = (quarterIndex & 1) == 0 ? Direction.EAST : Direction.WEST;
        var dirZ = (quarterIndex & 2) == 0 ? Direction.SOUTH : Direction.NORTH;
    
        Vec3i resultMultiplier = new Vec3i(dirX.getNormal().getX(), 1, dirZ.getNormal().getZ());
    
        BlockPos localBlockPos = new BlockPos(Math3D.divideByAxes(blockPos.subtract(centerBlock), resultMultiplier));
        
        Vec3 centerOfCenterBlockLocal = Vec3.atCenterOf(Vec3i.ZERO).subtract(0, offsetInnerDown, 0);
        Vec3i size = new Vec3i(sizes.get(dirX), innerHeight, sizes.get(dirZ)).offset(MIN_RADIUS, MIN_RADIUS, MIN_RADIUS);
        Vec3i sizeBig = size.offset(additiveStrength, additiveStrength, additiveStrength);
        
        Vec3 closestPoint = Math3D.getClosestPointOfBlock(localBlockPos, centerOfCenterBlockLocal).subtract(centerOfCenterBlockLocal);
        double posLocationClosest = (closestPoint.x * closestPoint.x) / (sizeBig.getX() * sizeBig.getX()) +
                (closestPoint.z * closestPoint.z) / (sizeBig.getZ() * sizeBig.getZ());
        boolean isClosestInside = posLocationClosest <= 1;
        
        return isClosestInside;
    }
    
    @Override
    public boolean isInside(ShapeBuilder shapeBuilder, Vec3i blockPos)
    {
        var centerBlock = shapeBuilder.getCenter();
        
        var sizes = shapeBuilder.getSizes();
        var additiveStrength = shapeBuilder.getStrength();
    
        var sizeDown = sizes.get(Direction.DOWN);
        var offsetInnerDown = sizeDown + ((MIN_HEIGHT - 1) / 2) - 1;
        var innerHeight = Math.max(sizeDown + sizes.get(Direction.UP) + MIN_HEIGHT - 2, 0);
    
        int minY = -offsetInnerDown - 1 - additiveStrength;
        int maxY = -offsetInnerDown + innerHeight + additiveStrength;
        
        var localBlockPos = blockPos.subtract(centerBlock);
        if(localBlockPos.getY() < minY || localBlockPos.getY() > maxY)
            return false;
        
        for(int quarterIndex = 0; quarterIndex < 4; ++quarterIndex)
            if(isInsideInfinityCylinder(shapeBuilder, blockPos, quarterIndex))
                return true;
        
        return false;
    }
}
