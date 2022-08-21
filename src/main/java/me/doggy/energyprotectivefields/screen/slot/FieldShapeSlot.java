package me.doggy.energyprotectivefields.screen.slot;

import me.doggy.energyprotectivefields.IFieldShape;
import me.doggy.energyprotectivefields.item.FieldShapeCubeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class FieldShapeSlot extends SlotItemHandler
{
    
    public FieldShapeSlot(IItemHandler itemHandler, int index, int xPosition, int yPosition)
    {
        super(itemHandler, index, xPosition, yPosition);
    }
    
    @Override
    public boolean mayPlace(@NotNull ItemStack stack)
    {
        return stack.isEmpty() || stack.getItem() instanceof IFieldShape;
    }
}
