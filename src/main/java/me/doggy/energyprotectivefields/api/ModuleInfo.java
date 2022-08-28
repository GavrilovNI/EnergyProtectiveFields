package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.api.module.IModule;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class ModuleInfo<T extends IModule>
{
    private final T module;
    private final int count;
    @Nullable
    private final Direction slotDirection;
    
    public ModuleInfo(T module, int count, @Nullable Direction slotDirection)
    {
        if(count <= 0)
            throw new IllegalArgumentException("Count should be positive.");
        this.module = module;
        this.count = count;
        this.slotDirection = slotDirection;
    }
    
    public ModuleInfo(T module, int count)
    {
        this(module, count, null);
    }
    
    public T getModule()
    {
        return module;
    }
    
    public int getCount()
    {
        return count;
    }
    
    @Nullable
    public Direction getSlotDirection()
    {
        return slotDirection;
    }
}
