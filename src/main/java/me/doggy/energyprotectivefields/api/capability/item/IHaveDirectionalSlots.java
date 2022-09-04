package me.doggy.energyprotectivefields.api.capability.item;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface IHaveDirectionalSlots
{
    @Nullable
    Direction getSlotDirection(int slot);
}
