package me.doggy.energyprotectivefields.controller;

import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FieldsDistributor
{
    private final IProjectorsProvider projectorsProvider;
    private final IFieldDistributingProjectorChooser projectorChooser;
    private final IFieldBlockProvider fieldBlockProvider;
    
    public FieldsDistributor(IProjectorsProvider projectorsProvider, IFieldDistributingProjectorChooser projectorChooser, IFieldBlockProvider fieldBlockProvider)
    {
        this.projectorsProvider = projectorsProvider;
        this.projectorChooser = projectorChooser;
        this.fieldBlockProvider = fieldBlockProvider;
    }
    
    public FieldsDistributor(IProjectorsProvider projectorsProvider, IFieldDistributingProjectorChooser projectorChooser, Level fieldBlocksLevel)
    {
        this.projectorsProvider = projectorsProvider;
        this.projectorChooser = projectorChooser;
        this.fieldBlockProvider = fieldPosition -> {
            if(fieldBlocksLevel.getBlockEntity(fieldPosition) instanceof FieldBlockEntity field)
                return field;
            return null;
        };
    }
    
    public void distributeField(BlockPos fieldPosition)
    {
        IFieldProjector projector = projectorChooser.getBestProjector(projectorsProvider.getProjectors(), fieldPosition);
        projector.getFields().add(fieldPosition);
    }
    
    public void distributeFields(Set<BlockPos> fieldPositions)
    {
        for(var blockPos : fieldPositions)
            distributeField(blockPos);
    }
    
    public void redistributeFieldsBetween(IFieldProjector from, IFieldProjector to)
    {
        var fields = from.getAllFieldsInShape();
        for(var blockPos : fields)
        {
            if(from.getEnergyToBuildField(blockPos) > to.getEnergyToBuildField(blockPos))
                transferField(blockPos, from, to);
        }
    }
    
    public void transferField(BlockPos fieldPosition, IFieldProjector from, IFieldProjector to)
    {
        from.getFields().remove(fieldPosition);
        var fieldBlock = fieldBlockProvider.getFieldBlock(fieldPosition);
        if(fieldBlock != null && fieldBlock.isMyProjector(from))
            fieldBlock.setProjectorPosition(to.getPosition());
        to.getFields().add(fieldPosition);
    }
    
    public void distributeFieldsFrom(IFieldProjector fieldProjector)
    {
        var fields = fieldProjector.getAllFieldsInShape();
        var projectors = projectorsProvider.getProjectors();
        for(var fieldPosition : fields)
        {
            IFieldProjector newProjector = projectorChooser.getBestProjector(projectors, fieldPosition);
            transferField(fieldPosition, fieldProjector, newProjector);
        }
        fieldProjector.getFields().clear();
    }
    
    public void redistributeFieldsForNew(IFieldProjector fieldProjector)
    {
        fieldProjector.getFields().clear();
        for(var otherProjector : projectorsProvider.getProjectors())
        {
            if(otherProjector == fieldProjector)
                continue;
            redistributeFieldsBetween(otherProjector, fieldProjector);
        }
    }
}
