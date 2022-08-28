package me.doggy.energyprotectivefields.api;

import com.google.common.collect.Multimap;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IFieldShapeChanger;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import net.minecraft.core.BlockPos;

import java.util.HashSet;

public class ShapeBuilder
{
    private final HashSet<BlockPos> positions = new HashSet<>();
    
    private final FieldControllerBlockEntity controller;
    private final Multimap<IModule, Integer> modules;
    
    private BlockPos center;
    private int size;
    private int strength;
    
    public ShapeBuilder(FieldControllerBlockEntity controller, Multimap<IModule, Integer> modules)
    {
        this.controller = controller;
        this.modules = modules;
        
        center = controller.getBlockPos();
        size = 0;
        strength = 0;
    }
    
    public ShapeBuilder init()
    {
        for(var entry : modules.entries())
            entry.getKey().applyOnInit(this, entry.getValue());
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
    
    public int getSize()
    {
        return size;
    }
    
    public ShapeBuilder setSize(int size)
    {
        if(size < 0)
            throw new IllegalArgumentException("Size must not be negative.");
        this.size = size;
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
        for(var module : modules.keys())
        {
            if(module instanceof IFieldShapeChanger fieldShapeChanger)
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
