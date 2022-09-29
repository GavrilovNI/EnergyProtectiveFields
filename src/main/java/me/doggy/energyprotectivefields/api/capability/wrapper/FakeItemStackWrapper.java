package me.doggy.energyprotectivefields.api.capability.wrapper;

import me.doggy.energyprotectivefields.api.capability.item.FakeItemsItemStackHandler;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FakeItemStackWrapper extends FakeItemsItemStackHandler implements ICapabilityProvider
{
    
    private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> this);
    
    public FakeItemStackWrapper(int size)
    {
        super(size);
    }
    
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side)
    {
        return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(capability, holder);
    }
}
