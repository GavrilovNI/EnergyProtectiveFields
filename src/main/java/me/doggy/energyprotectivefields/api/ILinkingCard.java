package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.api.module.IModule;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.data.WorldLinks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface ILinkingCard extends IModule
{
    @Nullable
    WorldLinks.LinkInfo getConnectionInfo(ItemStack itemStack);
    
    @Nullable
    FieldControllerBlockEntity findLinkedController(ItemStack itemStack, Level level, boolean onlyIfChunkIsLoaded);
    
    void linkToController(ItemStack itemStack, FieldControllerBlockEntity controller);
    void unlinkFromController(ItemStack itemStack);
    boolean isMyController(ItemStack itemStack, FieldControllerBlockEntity controller, boolean onlyIfChunkIsLoaded);
    
    @Override
    default int getLimitInMachineSlot(ItemStack itemStack)
    {
        return 1;
    }
}
