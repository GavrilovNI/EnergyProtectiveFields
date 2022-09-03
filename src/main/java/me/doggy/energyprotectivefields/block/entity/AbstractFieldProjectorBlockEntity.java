package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.IDestroyingHandler;
import me.doggy.energyprotectivefields.IServerTickable;
import me.doggy.energyprotectivefields.api.FieldSet;
import me.doggy.energyprotectivefields.api.ISwitchingHandler;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractFieldProjectorBlockEntity extends BlockEntity implements IFieldProjector, ISwitchingHandler, IDestroyingHandler, IServerTickable
{
    public static final int MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK = 100;
    public static final int MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK = 1000;
    public static final int DEFAULT_ENERGY_TO_BUILD_FIELD = 20;
    public static final int DEFAULT_ENERGY_TO_SUPPORT_FIELD = 4;
    
    protected final FieldSet fields = new FieldSet();
    protected final HashSet<BlockPos> fieldsToDestroy = new HashSet<>();
    
    protected int totalEnergyNeededToSupport = 0;
    
    public AbstractFieldProjectorBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(pType, pWorldPosition, pBlockState);
    }
    
    protected abstract BetterEnergyStorage getEnergyStorage();
    public abstract boolean isEnabled();
    protected abstract boolean canWork();
    
    protected int getMaxFieldCanBuildPerTick()
    {
        return MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK;
    }
    protected int getMaxFieldCanDestroyPerTick()
    {
        return MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK;
    }
    
    protected boolean canBuildIn(BlockPos position)
    {
        return level.isEmptyBlock(position);
    }
    
    protected boolean isImpossibleToBuildIn(BlockPos position)
    {
        return level.isOutsideBuildHeight(position);
    }
    
    protected boolean createFieldWithoutInitialization(BlockPos blockPos)
    {
        if(canBuildIn(blockPos) == false)
            return false;
    
        BetterEnergyStorage energyStorage = getEnergyStorage();
        
        int energyNeeded = getEnergyToBuildField(blockPos);
        if(energyStorage.consumeEnergy(energyNeeded, true) == energyNeeded)
        {
            boolean created = level.setBlock(blockPos, ModBlocks.FIELD_BLOCK.get().defaultBlockState(), 3);
            if(created)
            {
                energyStorage.consumeEnergy(energyNeeded, false);
                return true;
            }
        }
        return false;
    }
    
    protected boolean createField(BlockPos blockPos)
    {
        boolean created = createFieldWithoutInitialization(blockPos);
        if(created)
            initializeField(blockPos);
        return created;
    }
    
    protected void initializeField(BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity fieldBlockEntity)
            fieldBlockEntity.setProjectorPosition(worldPosition);
        else
            throw new IllegalStateException("FieldBlock not found in blockPos");
    }
    
    public void onFieldDestroyed(FieldBlockEntity fieldBlockEntity)
    {
        if(fieldBlockEntity.isRemoved() == false)
            throw new IllegalStateException("fieldBlockEntity is not destroyed.");
        if(fieldBlockEntity.isMyProjector(this) == false)
            throw new IllegalStateException("projector is not the owner of this fieldBlockEntity");
        
        BlockPos blockPos = fieldBlockEntity.getBlockPos();
    
        fieldsToDestroy.remove(blockPos);
        
        var state = fields.getState(blockPos);
    
        if(state != FieldSet.FieldState.Unknown)
        {
            if(state == FieldSet.FieldState.Created)
                totalEnergyNeededToSupport -= getEnergyToSupportField(blockPos);
            
            fields.setState(blockPos, FieldSet.FieldState.NotCreated);
        }
    }
    public void onFieldCreated(FieldBlockEntity fieldBlockEntity)
    {
        if(fieldBlockEntity.isMyProjector(this) == false)
            throw new IllegalStateException("fieldBlockEntity's projector is not this one");
        
        BlockPos blockPos = fieldBlockEntity.getBlockPos();
        var state = fields.getState(blockPos);
    
        if(state == FieldSet.FieldState.Creating)
        {
            totalEnergyNeededToSupport += getEnergyToSupportField(blockPos);
            fields.setState(blockPos, FieldSet.FieldState.Created);
        }
        else if(state == FieldSet.FieldState.Unknown)
        {
            fieldsToDestroy.add(blockPos);
        }
    }
    
    @Override
    public void clearFields()
    {
        fieldsToDestroy.addAll(fields.getFields(EnumSet.of(FieldSet.FieldState.Created, FieldSet.FieldState.Creating)));
        fields.clear();
    }
    
    
    public void removeFields(Collection<BlockPos> positions)
    {
        for(var blockPos : positions)
        {
            var state = fields.getState(blockPos);
            if(state != FieldSet.FieldState.Unknown)
            {
                if(state == FieldSet.FieldState.Created || state == FieldSet.FieldState.Creating)
                    fieldsToDestroy.add(blockPos);
                fields.remove(blockPos);
            }
        }
    }
    
    public void retainFields(Collection<BlockPos> positions)
    {
        var iterator = fields.iterator();
        while(iterator.hasNext())
        {
            var blockPos = iterator.next();
            if(positions.contains(blockPos) == false)
            {
                var state = iterator.getCurrentState();
                if(state == FieldSet.FieldState.Created || state == FieldSet.FieldState.Creating)
                    fieldsToDestroy.add(blockPos);
                iterator.remove();
            }
        }
    }
    
    @Override
    public Set<BlockPos> getAllFields()
    {
        return fields.getAll();
    }
    
    @Override
    public void removeFieldsIf(Predicate<BlockPos> predicate)
    {
        var iterator = fields.iterator();
        while(iterator.hasNext())
        {
            var blockPos = iterator.next();
            if(predicate.test(blockPos))
            {
                var state = iterator.getCurrentState();
                if(state == FieldSet.FieldState.Created || state == FieldSet.FieldState.Creating)
                    fieldsToDestroy.add(blockPos);
                iterator.remove();
            }
        }
    }
    
    @Override
    public void addField(BlockPos blockPos)
    {
        if(fields.contains(blockPos) == false)
        {
            if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity blockEntity && blockEntity.isMyProjector(this))
            {
                fields.setState(blockPos, FieldSet.FieldState.Creating);
                onFieldCreated(blockEntity);
            }
            else
            {
                fields.setState(blockPos, FieldSet.FieldState.NotCreated);
            }
        }
    }
    
    @Override
    public void removeField(BlockPos blockPos)
    {
        if(fields.contains(blockPos))
        {
            fields.remove(blockPos);
            fieldsToDestroy.add(blockPos);
        }
        else
        {
            if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity blockEntity && blockEntity.isMyProjector(this))
                fieldsToDestroy.add(blockPos);
        }
    }
    
    protected void destroyRequestedFields(int count)
    {
        for(var position : fieldsToDestroy.stream().limit(count).toList())
            level.removeBlock(position, false);
    }
    
    // returns true if have enough energy
    protected boolean consumeEnergyToSupportFields()
    {
        BetterEnergyStorage energyStorage = getEnergyStorage();
        var consumed = energyStorage.consumeEnergy(totalEnergyNeededToSupport, false);
        return consumed >= totalEnergyNeededToSupport;
    }
    
    protected void createFieldBlocks(int count)
    {
        var iterator = fields.iterator(FieldSet.FieldState.NotCreated);
        if(iterator.hasNext() == false)
        {
            fields.changeState(FieldSet.FieldState.NotCreatedTwice, FieldSet.FieldState.NotCreated);
            iterator = fields.iterator(FieldSet.FieldState.NotCreated);
        }
        
        while(count > 0 && iterator.hasNext())
        {
            var blockPos = iterator.next();
            
            if(isImpossibleToBuildIn(blockPos))
            {
                iterator.remove();
                continue;
            }
            else
            {
                iterator.setState(FieldSet.FieldState.Creating);
                boolean created = createField(blockPos);
                if(created)
                    count--;
                else
                    iterator.setState(FieldSet.FieldState.NotCreatedTwice);
            }
        }
    }
    
    protected void requestToDestroyAllCreatedFields()
    {
        var createdFields = fields.getFields(FieldSet.FieldState.Created, FieldSet.FieldState.Creating);
        fieldsToDestroy.addAll(createdFields);
    }
    
    @Deprecated // you should not remove all field blocks instantly
    protected void removeAllCreatedFieldBlocksInstantly()
    {
        requestToDestroyAllCreatedFields();
        destroyRequestedFields(fieldsToDestroy.size());
    }
    
    public void onDestroyed()
    {
        if(level.isClientSide() == false)
            removeAllCreatedFieldBlocksInstantly(); // TODO: should not remove instantly
    }
    
    @Override
    public int getEnergyToBuildField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 36)
        {
            return DEFAULT_ENERGY_TO_BUILD_FIELD;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(DEFAULT_ENERGY_TO_BUILD_FIELD * (distance - 5));
        }
    }
    
    @Override
    public int getEnergyToSupportField(BlockPos blockPos)
    {
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 36)
        {
            return DEFAULT_ENERGY_TO_SUPPORT_FIELD;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            return (int)(DEFAULT_ENERGY_TO_SUPPORT_FIELD * (distance - 5));
        }
    }
    
    @Override
    public void onDisabled()
    {
        if(level.isClientSide() == false)
            requestToDestroyAllCreatedFields();
        
    }
    
    @Override
    public void onEnabled()
    {
    
    }
    
    @Override
    public void onControllerDisabled()
    {
    
    }
    
    @Override
    public void onControllerEnabled()
    {
    
    }
    
    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos, BlockState blockState)
    {
        destroyRequestedFields(getMaxFieldCanDestroyPerTick());
        boolean hasEnoughEnergy = consumeEnergyToSupportFields();
    
        if(hasEnoughEnergy)
        {
            if(canWork())
                createFieldBlocks(getMaxFieldCanBuildPerTick());
        }
        else
        {
            requestToDestroyAllCreatedFields();
        }
    }
}
