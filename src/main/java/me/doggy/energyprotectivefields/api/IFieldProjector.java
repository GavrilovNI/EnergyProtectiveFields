package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface IFieldProjector extends IFieldStateListener
{
    boolean isEnabled();
    
    int getEnergyToBuildField(BlockPos blockPos);
    int getEnergyToSupportField(BlockPos blockPos);
    
    IFieldsContainer getFields();
    Set<BlockPos> getAllFieldsInShape();
    
    void queueFieldForCreatingIfInShape(BlockPos blockPos);
    
    void onControllerEnabled();
    void onControllerDisabled();
    
    Level getLevel();
    BlockPos getPosition();
    
    void setCamouflage(BlockState blockState);
}
