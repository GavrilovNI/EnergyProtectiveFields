package me.doggy.energyprotectivefields.api.capability.item;

import me.doggy.energyprotectivefields.api.module.field.IDirectionalFieldModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldShape;
import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.api.utils.InventoryHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FieldControllerItemStackHandler extends ItemStackHandler implements IHaveDirectionalSlots
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
        
        Class<? extends IModule> classNeeded;
        if(isShapeSlot(slot))
            classNeeded = IFieldShape.class;
        else if(isDirectionalSlot(slot))
            classNeeded = IDirectionalFieldModule.class;
        else if(isOtherSlot(slot))
            classNeeded = IModule.class;
        else
            return false;
        
        return InventoryHelper.getStackAs(itemStack, classNeeded) != null;
    }
    
    private Optional<Integer> tryFindLimit(ItemStack itemStack)
    {
        IFieldModule module = InventoryHelper.getStackAs(itemStack, IFieldModule.class);
        if(module != null)
            return Optional.of(module.getLimitInControllerSlot(itemStack));
        return Optional.empty();
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
