package me.doggy.energyprotectivefields.item;

import me.doggy.energyprotectivefields.ILinkingCard;
import me.doggy.energyprotectivefields.api.BlockLocation;
import me.doggy.energyprotectivefields.block.entity.FieldControllerBlockEntity;
import me.doggy.energyprotectivefields.data.WorldLinks;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class LinkingCardItem extends Item implements ILinkingCard
{
    public LinkingCardItem(Properties pProperties)
    {
        super(pProperties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext pContext)
    {
        var level = pContext.getLevel();
        if(level.isClientSide() == false)
        {
            var blockPos = pContext.getClickedPos();
            
            if(level.getBlockEntity(blockPos) instanceof FieldControllerBlockEntity blockEntity)
            {
                var player = pContext.getPlayer();
                
                var itemStack = pContext.getItemInHand();
                
                var count = itemStack.getCount();
                if(count > 1)
                {
                    var newItemStack = itemStack.copy();
                    newItemStack.setCount(1);
                    itemStack.setCount(count - 1);
                    
                    itemStack = newItemStack;
                }
                
                linkToController(itemStack, blockEntity);
                player.addItem(itemStack);
            }
        }
        
        return super.useOn(pContext);
    }
    
    @Nullable
    public FieldControllerBlockEntity findLinkedController(Level level, ItemStack itemStack)
    {
        var tag = itemStack.getTagElement("target");
    
        if(tag == null)
            return null;
    
        var blockLocation = BlockLocation.fromNBT(tag);
        var controllerId = tag.get("id");
    
        var levelRegistryName = blockLocation.getLevelRegistryName();
        
        if(level.dimension().getRegistryName().equals(levelRegistryName) == false)
            return null;
        
        if(level.getBlockEntity(blockLocation) instanceof FieldControllerBlockEntity blockEntity)
        {
            if(blockEntity.getUUID().equals(controllerId))
                return blockEntity;
        }
        
        return null;
    }
    
    @Nullable
    @Override
    public WorldLinks.LinkInfo getConnectionInfo(ItemStack itemStack)
    {
        var tag = itemStack.getTagElement("target");
    
        if(tag == null)
            return null;
    
        var blockLocation = BlockLocation.fromNBT(tag);
        var controllerId = tag.getUUID("uuid");
        return new WorldLinks.LinkInfo(blockLocation, controllerId);
    }
    
    @Nullable
    @Override
    public FieldControllerBlockEntity findLinkedController(ItemStack itemStack, Level level, boolean onlyIfChunkIsLoaded)
    {
        var tag = itemStack.getTagElement("target");
    
        if(tag == null)
            return null;
    
        var blockLocation = BlockLocation.fromNBT(tag);
        var controllerId = tag.getUUID("uuid");
    
        var levelRegistryName = blockLocation.getLevelRegistryName();
    
        if(level.dimension().getRegistryName().equals(levelRegistryName) == false)
            return null;
        
        if(onlyIfChunkIsLoaded && level.hasChunkAt(blockLocation) == false)
            return null;
    
        if(level.getBlockEntity(blockLocation) instanceof FieldControllerBlockEntity blockEntity)
        {
            if(blockEntity.getUUID().equals(controllerId))
                return blockEntity;
        }
    
        return null;
    }
    
    @Override
    public void linkToController(ItemStack itemStack, FieldControllerBlockEntity blockEntity)
    {
        BlockLocation blockLocation = new BlockLocation(blockEntity.getLevel(), blockEntity.getBlockPos());
        var nbt = blockLocation.serializeNBT();
        nbt.putUUID("uuid", blockEntity.getUUID());
        itemStack.addTagElement("target", nbt);
    }
    
    @Override
    public void unlinkFromController(ItemStack itemStack)
    {
        itemStack.removeTagKey("target");
    }
    
    @Override
    public boolean isMyController(ItemStack itemStack, @Nullable FieldControllerBlockEntity controller, boolean onlyIfChunkIsLoaded)
    {
        return controller != null && controller == findLinkedController(itemStack, controller.getLevel(), onlyIfChunkIsLoaded);
    }
}
