package me.doggy.energyprotectivefields.controller;

import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

public interface IFieldBlockProvider
{
    @Nullable
    FieldBlockEntity getFieldBlock(BlockPos fieldPosition);
}
