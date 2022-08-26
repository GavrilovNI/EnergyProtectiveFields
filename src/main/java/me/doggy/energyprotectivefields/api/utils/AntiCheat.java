package me.doggy.energyprotectivefields.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class AntiCheat
{
    //from ServerGamePacketListenerImpl.handleUseItemOn
    public static boolean canReachAndInteract(ServerPlayer player, ServerLevel level, BlockPos blockPos)
    {
        double maxDistSqr = player.getAttribute(net.minecraftforge.common.ForgeMod.REACH_DISTANCE.get()).getValue() + 3;
        maxDistSqr *= maxDistSqr;
        
        var realDistSqr = player.distanceToSqr((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D);
        
        return realDistSqr < maxDistSqr && level.mayInteract(player, blockPos);
    }
}
