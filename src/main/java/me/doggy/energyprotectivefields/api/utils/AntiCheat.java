package me.doggy.energyprotectivefields.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class AntiCheat
{
    //from ServerGamePacketListenerImpl.handleUseItemOn
    public static boolean canReachAndInteract(ServerPlayer player, ServerLevel level, BlockPos blockPos)
    {
        BlockHitResult blockHitResult = new BlockHitResult(player.position(), player.getDirection(), blockPos, false);
        PlayerInteractEvent event = ForgeHooks.onRightClickBlock(player, InteractionHand.MAIN_HAND, blockPos, blockHitResult);
        if(event.isCanceled())
            event = ForgeHooks.onRightClickBlock(player, InteractionHand.OFF_HAND, blockPos, blockHitResult);
        if(event.isCanceled())
            return false;
        
        double maxDistSqr = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue() + 3;
        maxDistSqr *= maxDistSqr;
        
        var realDistSqr = player.distanceToSqr((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
        
        return realDistSqr < maxDistSqr && level.mayInteract(player, blockPos);
    }
}
