package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public interface ILinkable extends IHaveUUID
{
    void onLinked(ServerLevel level, BlockPos blockPos);
    void onUnlinked(ServerLevel level, BlockPos blockPos);
}
