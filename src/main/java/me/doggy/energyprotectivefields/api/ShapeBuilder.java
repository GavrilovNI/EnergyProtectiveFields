package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.module.field.IFieldShapeValidator;
import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.controller.IFieldBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShapeBuilder
{
    private final HashSet<BlockPos> positions = new HashSet<>();
    
    private final FieldControllerBlockEntity controller;
    private final Collection<ModuleInfo<IFieldModule>> modules;
    
    private final HashMap<Direction, Integer> sizes = new HashMap<>();
    private BlockPos center;
    private int strength;
    
    private final HashMap<Direction, Integer> rotations = new HashMap<>(Map.of(
            Direction.EAST, 0,
            Direction.UP, 0,
            Direction.SOUTH, 0
    ));
    
    
    private static final List<Direction> positiveDirections = List.of(Direction.EAST, Direction.UP, Direction.SOUTH);
    private static final List<Direction.Axis> axes = List.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z);
    
    private final int[] rotatedMultipliers = {1, 1, 1};
    private final Direction.Axis[] rotatedAxisByIndex = axes.toArray(Direction.Axis[]::new);
    
    private BoundingBox bounds = null;
    
    public ShapeBuilder(FieldControllerBlockEntity controller, Collection<ModuleInfo<IFieldModule>> modules)
    {
        this.controller = controller;
        this.modules = modules;
        
        center = controller.getBlockPos();
        strength = 0;
    }
    
    public ShapeBuilder init()
    {
        for(var moduleInfo : modules)
            moduleInfo.getModule().applyOnInit(this, moduleInfo);
        return this;
    }
    
    public BoundingBox getBounds()
    {
        if(bounds == null)
            return null;
        return new BoundingBox(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ());
    }
    
    public boolean hasModule(Class<? extends IModule> clazz)
    {
        for(var moduleInfo : modules)
            if(clazz.isAssignableFrom(moduleInfo.getModule().getClass()))
                return true;
        return false;
    }
    
    public ArrayList<ModuleInfo<IFieldModule>> getModules(Class<IModule> clazz)
    {
        ArrayList<ModuleInfo<IFieldModule>> result = new ArrayList<>();
    
        for(var moduleInfo : modules)
            if(clazz.isAssignableFrom(moduleInfo.getModule().getClass()))
                result.add(moduleInfo);
        
        return result;
    }
    
    public FieldControllerBlockEntity getController()
    {
        return controller;
    }
    
    public int getRotation(Direction direction)
    {
        if(direction.getAxisDirection() == Direction.AxisDirection.POSITIVE)
            return rotations.get(direction);
        else
            return -rotations.get(direction.getOpposite());
    }
    
    public void setRotation(Direction direction, int value)
    {
        if(direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE)
        {
            direction = direction.getOpposite();
            value = -value;
        }
        
        value %= 4;
        if(value < 0)
            value += 4;
    
        rotations.put(direction, value);
        prepareRotation();
    }
    
    private void prepareRotation()
    {
        Direction[] rotatedDirections = positiveDirections.toArray(Direction[]::new);
        
        for(var a = 0; a < 3; ++a)
        {
            var axisDirection = positiveDirections.get(a);
            var axis = axisDirection.getAxis();
            for(int r = 0; r < getRotation(axisDirection); ++r)
            {
                rotatedDirections[0] = rotatedDirections[0].getClockWise(axis);
                rotatedDirections[1] = rotatedDirections[1].getClockWise(axis);
                rotatedDirections[2] = rotatedDirections[2].getClockWise(axis);
            }
        }
        
        for(int indexWas = 0; indexWas < 3; ++indexWas)
        {
            boolean isPositive = rotatedDirections[indexWas].getAxisDirection() == Direction.AxisDirection.POSITIVE;
            
            Direction directionToFind;
            int multiplier;
            if(isPositive)
            {
                multiplier = 1;
                directionToFind = rotatedDirections[indexWas];
            }
            else
            {
                multiplier = -1;
                directionToFind = rotatedDirections[indexWas].getOpposite();
            }
            
            int indexNow = positiveDirections.indexOf(directionToFind);
            
            rotatedAxisByIndex[indexNow] = axes.get(indexWas);
            rotatedMultipliers[indexNow] = multiplier;
        }
    }
    
    public BlockPos rotateVector(Vec3i vector)
    {
        return new BlockPos(
                vector.get(rotatedAxisByIndex[0]) * rotatedMultipliers[0],
                vector.get(rotatedAxisByIndex[1]) * rotatedMultipliers[1],
                vector.get(rotatedAxisByIndex[2]) * rotatedMultipliers[2]
        );
    }
    
    public BlockPos rotateVectorBack(Vec3i vector)
    {
        int[] values = { vector.getX() / rotatedMultipliers[0], vector.getY() / rotatedMultipliers[1], vector.getZ() / rotatedMultipliers[2] };
        int[] newValues = { vector.getX() / rotatedMultipliers[0], vector.getY() / rotatedMultipliers[1], vector.getZ() / rotatedMultipliers[2] };
        
        for(int i = 0; i < 3; ++i)
        {
            var axisIndex = axes.indexOf(rotatedAxisByIndex[i]);
            newValues[axisIndex] = values[i];
        }
        
        return new BlockPos(newValues[0], newValues[1], newValues[2]);
    }
    
    public BlockPos getCenter()
    {
        return center;
    }
    
    public ShapeBuilder setCenter(BlockPos blockPos)
    {
        this.center = blockPos;
        return this;
    }
    
    public int getSize(Direction direction)
    {
        return sizes.getOrDefault(direction, 0);
    }
    
    public Map<Direction, Integer> getSizes()
    {
        var result = new HashMap<>(sizes);
        for(var direction : Direction.values())
            result.put(direction, result.getOrDefault(direction, 0));
        return result;
    }
    
    public ShapeBuilder setSize(int size)
    {
        if(size < 0)
            throw new IllegalArgumentException("Size must not be negative.");
        for(var direction : Direction.values())
            this.sizes.put(direction, size);
        return this;
    }
    
    public ShapeBuilder setSize(@Nullable Direction direction, int size)
    {
        if(direction == null)
        {
            setSize(size);
            return this;
        }
        
        if(size < 0)
            throw new IllegalArgumentException("Size must not be negative.");
        this.sizes.put(direction, size);
        return this;
    }
    
    public int getStrength()
    {
        return strength;
    }
    
    public ShapeBuilder setStrength(int strength)
    {
        if(strength < 0)
            throw new IllegalArgumentException("Strength must not be negative.");
        this.strength = strength;
        return this;
    }
    
    protected boolean validateBlockPos(Vec3i blockPos)
    {
        for(var moduleInfo : modules)
        {
            if(moduleInfo.getModule() instanceof IFieldShapeValidator fieldShapeChanger)
                if(fieldShapeChanger.isInShape(this, blockPos) == false)
                    return false;
        }
        return true;
    }
    
    public ShapeBuilder addFieldByVector(BlockPos vector)
    {
        var blockPos = rotateVector(vector).offset(getCenter());
        return addField(blockPos);
    }
    
    public ShapeBuilder addField(BlockPos blockPos)
    {
        if(validateBlockPos(blockPos) == false)
            return this;
        
        positions.add(blockPos);
        
        if(bounds == null)
            bounds = new BoundingBox(blockPos);
        else
            bounds.encapsulate(blockPos);
        return this;
    }
    
    public boolean isInsideField(IFieldShape shape, Vec3i pos)
    {
        if(bounds == null)
            return false;
        
        if(bounds.isInside(pos) && shape.isInside(this, rotateVectorBack(pos.subtract(center)).offset(center)) && validateBlockPos(pos))
            return true;
        
        return false;
    }
    
    public ShapeBuilder addFields(IFieldShape shape)
    {
        shape.addFields(this);
        return this;
    }
    
    public Set<BlockPos> build()
    {
        return Collections.unmodifiableSet(positions);
    }
}
