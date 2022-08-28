package me.doggy.energyprotectivefields.data.handler;

import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.module.IDirectionalShapeModule;
import me.doggy.energyprotectivefields.api.module.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.IShapeModule;
import me.doggy.energyprotectivefields.api.utils.ItemStackConvertor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class FieldControllerItemStackHandler extends ItemStackHandler
{
    public static final int SLOT_FIELD_SHAPE  = 0;
    
    public static final int SHAPE_SLOTS_COUNT = 1;
    public static final int DIRECTIONAL_MODULES_SLOTS_COUNT = 12;
    public static final int OTHER_MODULES_SLOTS_COUNT = 6;
    
    public static final int SIZE = SHAPE_SLOTS_COUNT + DIRECTIONAL_MODULES_SLOTS_COUNT + OTHER_MODULES_SLOTS_COUNT;
    
    public FieldControllerItemStackHandler()
    {
        super(SIZE);
    }
    
    public boolean isShapeSlot(int slot)
    {
        return slot >= 0 && slot < SHAPE_SLOTS_COUNT;
    }
    
    public boolean isDirectionalSlot(int slot)
    {
        return slot >= SHAPE_SLOTS_COUNT && slot < SHAPE_SLOTS_COUNT + DIRECTIONAL_MODULES_SLOTS_COUNT;
    }
    
    public boolean isOtherSlot(int slot)
    {
        return slot >= SHAPE_SLOTS_COUNT + DIRECTIONAL_MODULES_SLOTS_COUNT && slot < SIZE;
    }
    
    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack itemStack)
    {
        if(itemStack.isEmpty())
            return true;
        
        Class<? extends IShapeModule> classNeeded;
        if(isShapeSlot(slot))
            classNeeded = IFieldShape.class;
        else if(isDirectionalSlot(slot))
            classNeeded = IDirectionalShapeModule.class;
        else if(isOtherSlot(slot))
            classNeeded = IShapeModule.class;
        else
            return false;
        
        return ItemStackConvertor.getAs(itemStack, classNeeded) != null;
    }
    
    private Optional<Integer> tryFindLimit(ItemStack itemStack)
    {
        IShapeModule module = ItemStackConvertor.getAs(itemStack, IShapeModule.class);
        if(module != null)
            return Optional.of(module.getLimitInControllerSlot(itemStack));
        return Optional.empty();
    }
    
    @Nullable
    public IFieldShape getShape()
    {
        return ItemStackConvertor.getAs(getStackInSlot(FieldControllerItemStackHandler.SLOT_FIELD_SHAPE), IFieldShape.class);
    }
    
    public<T extends IModule> ModuleInfo<T> getModuleInfo(int slot, Class<T> clazz)
    {
        var moduleStack = getStackInSlot(slot);
        var module = ItemStackConvertor.getAs(moduleStack, clazz);
        return module == null ? null : new ModuleInfo<T>(module, moduleStack.getCount(), getSlotDirection(slot));
    }
    
    public<T extends IModule> ArrayList<ModuleInfo<T>> getModulesInfo(Class<T> clazz)
    {
        ArrayList<ModuleInfo<T>> modules = new ArrayList<>();
        
        for(int i = 0; i < getSlots(); ++i)
        {
            var info = getModuleInfo(i, clazz);
            if(info != null)
                modules.add(info);
        }
        return modules;
    }
    
    @Nullable
    public Direction getSlotDirection(int slot)
    {
        return switch(slot)
                {
                    case 1,4 -> Direction.NORTH;
                    case 2,3 -> Direction.UP;
                    case 5,7 -> Direction.WEST;
                    case 6,8 -> Direction.EAST;
                    case 9,12 -> Direction.SOUTH;
                    case 10,11 -> Direction.DOWN;
                    default -> null;
                };
    }
    
    @Deprecated // use getStackLimit instead
    @Override
    public int getSlotLimit(int slot)
    {
        var itemStack = getStackInSlot(slot);
        return tryFindLimit(itemStack).orElse(super.getSlotLimit(slot));
    }
    
    @Override
    protected int getStackLimit(int slot, @NotNull ItemStack stack)
    {
        return tryFindLimit(stack).orElse(super.getStackLimit(slot, stack));
    }
}
