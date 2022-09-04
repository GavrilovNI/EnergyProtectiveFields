package me.doggy.energyprotectivefields.api.capability.item;

import me.doggy.energyprotectivefields.api.module.field.IFieldModule;
import me.doggy.energyprotectivefields.api.utils.InventoryHelper;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ModulesItemStackHandler extends ItemStackHandler implements IHaveDirectionalSlots
{
    public ModulesItemStackHandler(int size)
    {
        super(size);
    }
    
    @Override
    public @Nullable Direction getSlotDirection(int slot)
    {
        return null;
    }
    
    private Optional<Integer> tryFindLimit(ItemStack itemStack)
    {
        IFieldModule module = InventoryHelper.getStackAs(itemStack, IFieldModule.class);
        if(module != null)
            return Optional.of(module.getLimitInMachineSlot(itemStack));
        return Optional.empty();
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
