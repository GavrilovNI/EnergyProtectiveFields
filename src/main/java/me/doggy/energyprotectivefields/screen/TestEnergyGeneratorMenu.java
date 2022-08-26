package me.doggy.energyprotectivefields.screen;

import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.block.entity.InfinityEnergyGeneratorBlockEntity;
import me.doggy.energyprotectivefields.networking.NetworkManager;
import me.doggy.energyprotectivefields.networking.packet.TestEnergyGeneratorSetMaxExtractC2SPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TestEnergyGeneratorMenu extends AbstractContainerWithEnergyMenu
{
    private final InfinityEnergyGeneratorBlockEntity blockEntity;
    private final Level level;
    
    public TestEnergyGeneratorMenu(int pContainerId, Inventory inventory, FriendlyByteBuf extraData)
    {
        this(pContainerId, inventory, inventory.player.level.getBlockEntity(extraData.readBlockPos()));
    }
    
    public TestEnergyGeneratorMenu(int pContainerId, Inventory inventory, BlockEntity blockEntity)
    {
        super(ModMenuTypes.TEST_ENERGY_GENERATOR_MENU.get(), pContainerId, inventory.player, blockEntity);
        this.blockEntity = (InfinityEnergyGeneratorBlockEntity) blockEntity;
        this.level = inventory.player.level;
    }
    
    public int getMaxEnergyExtract()
    {
        return blockEntity.getMaxEnergyExtract();
    }
    public void setMaxEnergyExtract(int maxExtract)
    {
        if(level.isClientSide())
            NetworkManager.sendToServer(new TestEnergyGeneratorSetMaxExtractC2SPacket(maxExtract, blockEntity.getBlockPos()));
    }
    
    @Override
    public boolean stillValid(Player pPlayer)
    {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.INFINITY_ENERGY_GENERATOR.get());
    
    }
    
    @Override
    public int getSlotsCount()
    {
        return 0;
    }
}
