package me.doggy.energyprotectivefields.api.utils;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemStackConvertor
{
    public static <T> T getAs(ItemStack itemStack, Class<T> clazz)
    {
        ItemLike itemLike = itemStack.getItem();
        if(clazz.isAssignableFrom(itemLike.getClass()))
            return (T)itemLike;
        if(itemLike instanceof BlockItem blockItem)
        {
            var block = blockItem.getBlock();
            if(clazz.isAssignableFrom(block.getClass()))
                return (T)block;
        }
        return null;
    }
}
