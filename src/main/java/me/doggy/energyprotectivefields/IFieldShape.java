package me.doggy.energyprotectivefields;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Set;

public interface IFieldShape
{
    Set<BlockPos> getShieldPoses(BlockPos center, int sizeUpgrade);
}
