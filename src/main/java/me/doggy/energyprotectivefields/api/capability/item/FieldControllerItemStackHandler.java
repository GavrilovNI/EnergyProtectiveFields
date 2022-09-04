package me.doggy.energyprotectivefields.api.capability.item;

import me.doggy.energyprotectivefields.api.module.field.IDirectionalFieldModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.utils.InventoryHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FieldControllerItemStackHandler extends ModulesItemStackHandler implements IHaveDirectionalSlots
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

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack itemStack)
    {
        if(slot < 0 || slot >= SIZE)
            return false;
        if(itemStack.isEmpty())
            return true;
        
        Class<? extends IModule> classNeeded;
        if(slot < SHAPE_SLOTS_COUNT)
            classNeeded = IFieldShape.class;
        else if(slot < SHAPE_SLOTS_COUNT + DIRECTIONAL_MODULES_SLOTS_COUNT)
            classNeeded = IDirectionalFieldModule.class;
        else
            classNeeded = IModule.class;
        
        return InventoryHelper.getStackAs(itemStack, classNeeded) != null;
    }
    
    @Nullable
    public IFieldShape getShape()
    {
        return InventoryHelper.getStackAs(getStackInSlot(FieldControllerItemStackHandler.SLOT_FIELD_SHAPE), IFieldShape.class);
    }
    
    @Override
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
}
