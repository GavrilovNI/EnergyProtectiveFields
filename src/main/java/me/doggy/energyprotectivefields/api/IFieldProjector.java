package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

public interface IFieldProjector extends IFieldStateListener
{
    boolean isEnabled();
    
    int getEnergyToBuildField(BlockPos blockPos);
    int getEnergyToSupportField(BlockPos blockPos);
    
    void clearFields();
    void addField(BlockPos blockPos);
    void removeField(BlockPos blockPos);
    
    void removeFields(Collection<BlockPos> positions);
    void retainFields(Collection<BlockPos> positions);
    void removeFieldsIf(Predicate<BlockPos> predicate);
    
    void onControllerEnabled();
    void onControllerDisabled();
    
    Level getLevel();
    BlockPos getPosition();
    
    Set<BlockPos> getAllFieldsInShape();
}
