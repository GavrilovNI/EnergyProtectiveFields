package me.doggy.energyprotectivefields.item.module.field;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.ModuleInfo;
import me.doggy.energyprotectivefields.api.capability.wrapper.FakeItemStackWrapper;
import me.doggy.energyprotectivefields.api.module.projector.IProjectorModule;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.item.ModItems;
import me.doggy.energyprotectivefields.screen.CamouflageModuleMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CamouflageModuleItem extends Item implements IProjectorModule
{
    public static final BlockState DEFAULT_STATE = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
    
    public CamouflageModuleItem(Properties pProperties)
    {
        super(pProperties.stacksTo(1));
    }
    
    public BlockState getBlockStateToRender(ItemStack itemStack)
    {
        BlockState blockState = DEFAULT_STATE;
        
        var lazy = itemStack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if(lazy.isPresent())
        {
            var handler = lazy.orElseThrow(() -> new IllegalStateException("RenderModuleItem itemStack capability not found."));
            if(handler.getSlots() > 0)
            {
                ItemStack stackInSlot = handler.getStackInSlot(0);
                if(stackInSlot.getItem() instanceof BlockItem blockItem)
                    blockState = blockItem.getBlock().defaultBlockState();
            }
        }
        
        return blockState;
    }
    
    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt)
    {
        var wrapper = new FakeItemStackWrapper(1) {
            @Override
            public boolean isItemValid(int slot, @NotNull ItemStack stack)
            {
                return stack.getItem() instanceof BlockItem;
            }
        };
        if(nbt != null)
            wrapper.deserializeNBT(nbt);
        return wrapper;
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand)
    {
        int slotIndex;
        if (pUsedHand == InteractionHand.MAIN_HAND)
            slotIndex = pPlayer.getInventory().selected;
        else
            slotIndex = Inventory.SLOT_OFFHAND;
    
        ItemStack itemstack = pPlayer.getInventory().getItem(slotIndex);
        if(itemstack.is(this) == false)
            return InteractionResultHolder.fail(itemstack);
        
        if(pPlayer instanceof ServerPlayer serverPlayer)
        {
            NetworkHooks.openGui(serverPlayer, new MenuProvider()
            {
                @Override
                public Component getDisplayName()
                {
                    var id = ModItems.CAMOUFLAGE_MODULE.getId();
                    return new TranslatableComponent("item." + id.getNamespace() + "." + id.getPath());
                }
    
                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
                {
                    return new CamouflageModuleMenu(pContainerId, pInventory, slotIndex);
                }
            }, buf -> buf.writeInt(slotIndex));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());
    }
    
    @Override
    public void cancel(ModuleInfo<IProjectorModule> moduleModuleInfo, IFieldProjector projector)
    {
        projector.setCamouflage(DEFAULT_STATE);
    }
    
    @Override
    public void apply(ModuleInfo<IProjectorModule> moduleModuleInfo, IFieldProjector projector)
    {
        projector.setCamouflage(getBlockStateToRender(moduleModuleInfo.getItemStack()));
    }
}
