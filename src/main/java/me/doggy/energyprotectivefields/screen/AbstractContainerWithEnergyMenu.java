package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.networking.NetworkManager;
import me.doggy.energyprotectivefields.networking.packet.UpdateEnergyCapabilityInBlockEntityS2CPacket;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractContainerWithEnergyMenu extends BaseItemInventoryMenu
{
    private final Player player;
    private final BlockEntity blockEntity;
    private IEnergyStorage sentStorage = null;
    
    protected AbstractContainerWithEnergyMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Player player, BlockEntity blockEntity)
    {
        super(pMenuType, pContainerId);
        this.player = player;
        this.blockEntity = blockEntity;
    }
    
    protected boolean isNeededToSave(IEnergyStorage energyStorage)
    {
        return (sentStorage == null && energyStorage != null) || sentStorage.equals(energyStorage) == false;
    }
    
    protected void sendEnergyDataToPlayer(IEnergyStorage energyStorage)
    {
        if(player instanceof ServerPlayer serverPlayer)
        {
            if(energyStorage instanceof BetterEnergyStorage serializableEnergyStorage)
            {
                NetworkManager.sendToPlayer(new UpdateEnergyCapabilityInBlockEntityS2CPacket(blockEntity.getLevel(), blockEntity.getBlockPos(), serializableEnergyStorage), serverPlayer);
                setSentStorage(serializableEnergyStorage.copy());
            }
            else
            {
                throw new UnsupportedOperationException("Got unserializable energy storage.");
            }
        }
    }
    
    protected void setSentStorage(IEnergyStorage sentStorageCopy)
    {
        this.sentStorage = sentStorageCopy;
    }
    
    @Nullable
    protected IEnergyStorage getEnergyStorage()
    {
        var capability = blockEntity.getCapability(CapabilityEnergy.ENERGY);
        return capability.orElse(null);
    }
    
    @Override
    public void broadcastChanges()
    {
        super.broadcastChanges();
    
        var energyStorage = getEnergyStorage();
        if(isNeededToSave(energyStorage))
            sendEnergyDataToPlayer(energyStorage);
    }
    
    @Override
    public void sendAllDataToRemote()
    {
        super.sendAllDataToRemote();
        var energyStorage = getEnergyStorage();
        sendEnergyDataToPlayer(energyStorage);
    }
}
