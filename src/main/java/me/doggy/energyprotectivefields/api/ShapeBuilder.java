package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IFieldShapeChanger;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.IShapeModule;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShapeBuilder
{
    private final HashSet<BlockPos> positions = new HashSet<>();
    
    private final FieldControllerBlockEntity controller;
    private final Collection<ModuleInfo<IShapeModule>> modules;
    
    private final HashMap<Direction, Integer> sizes = new HashMap<>();
    private BlockPos center;
    private int strength;
    
    public ShapeBuilder(FieldControllerBlockEntity controller, Collection<ModuleInfo<IShapeModule>> modules)
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
    
    public FieldControllerBlockEntity getController()
    {
        return controller;
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
    
    public ShapeBuilder addField(BlockPos blockPos)
    {
        for(var moduleInfo : modules)
        {
            if(moduleInfo.getModule() instanceof IFieldShapeChanger fieldShapeChanger)
                if(fieldShapeChanger.isInShape(this, blockPos) == false)
                    return this;
        }
        
        positions.add(blockPos);
        return this;
    }
    
    public ShapeBuilder addFields(IFieldShape shape)
    {
        shape.addFields(this);
        return this;
    }
    
    public HashSet<BlockPos> build()
    {
        return new HashSet<>(positions);
    }
}
