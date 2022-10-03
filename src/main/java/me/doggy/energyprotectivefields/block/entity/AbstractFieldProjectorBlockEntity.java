package me.doggy.energyprotectivefields.block.entity;

import me.doggy.energyprotectivefields.IDestroyingHandler;
import me.doggy.energyprotectivefields.IServerTickable;
import me.doggy.energyprotectivefields.api.FieldSet;
import me.doggy.energyprotectivefields.api.ISwitchingHandler;
import me.doggy.energyprotectivefields.api.IFieldProjector;
import me.doggy.energyprotectivefields.api.capability.energy.BetterEnergyStorage;
import me.doggy.energyprotectivefields.api.utils.HashCounter;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.ModBlocks;
import me.doggy.energyprotectivefields.data.WorldChunkChanges;
import me.doggy.energyprotectivefields.data.WorldChunksLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractFieldProjectorBlockEntity extends EnergizedBlockEntity implements IFieldProjector, ISwitchingHandler, IDestroyingHandler, IServerTickable
{
    public static final int MAX_FIELD_BLOCKS_CAN_BUILD_PER_TICK = 100;
    public static final int MAX_FIELD_BLOCKS_CAN_REMOVE_PER_TICK = 1000;
    public static final int DEFAULT_ENERGY_TO_BUILD_FIELD = 10;
    public static final int DEFAULT_ENERGY_TO_SUPPORT_FIELD = 2;
    
    protected final FieldSet fields = new FieldSet();
    protected final HashSet<BlockPos> fieldsToDestroy = new HashSet<>();
    protected final HashMap<BlockPos, Integer> cashedEnergyToBuild = new HashMap<>();
    
    protected final HashCounter<ChunkPos> fieldsByChunkCounter = new HashCounter<>();
    protected final Map<BlockPos, Long> lastFailedAttemptsToCreateField = new HashMap<>();
    
    protected BlockState camouflageBlockState = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
    
    protected int totalEnergyNeededToSupport = 0;
    
    public AbstractFieldProjectorBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState, BetterEnergyStorage defaultEnergyStorage)
    {
        super(pType, pWorldPosition, pBlockState, defaultEnergyStorage);
    }
    
    public boolean hasCamouflage()
    {
        return camouflageBlockState.isAir() == false && camouflageBlockState.equals(ModBlocks.FIELD_BLOCK.get().defaultBlockState()) == false;
    }
    
    public void setCamouflage(BlockState blockState)
    {
        if(blockState.isAir())
            blockState = ModBlocks.FIELD_BLOCK.get().defaultBlockState();
        
        this.camouflageBlockState = blockState;
        for(var fieldPosition : fields.getFields(FieldSet.FieldState.Created))
        {
            if(level.isLoaded(fieldPosition))
            {
                ((FieldBlockEntity)level.getBlockEntity(fieldPosition)).setCamouflage(camouflageBlockState);
            }
        }
    }
    
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
    
    protected boolean createFieldWithoutInitialization(BlockPos fieldPosition, boolean forced)
    {
        boolean shouldTryToCreate = forced || shouldTryToCreateField(fieldPosition);
        
        if(shouldTryToCreate == false)
            return false;
        
        boolean created = false;
        
        BetterEnergyStorage energyStorage = getEnergyStorage();
        int energyToBuild = getEnergyToBuildField(fieldPosition);
        boolean hasEnoughEnergy = energyStorage.consumeEnergy(energyToBuild, true) >= energyToBuild;
        
        if(hasEnoughEnergy)
        {
            if(canBuildIn(fieldPosition))
            {
                var fieldBlockState = ModBlocks.FIELD_BLOCK.get().defaultBlockState()
                        .setValue(FieldBlock.RENDERING_ITSELF, hasCamouflage() == false);
                
                created = level.setBlock(fieldPosition, fieldBlockState, 3);
                if(created)
                {
                    cashedEnergyToBuild.remove(fieldPosition);
                    energyStorage.consumeEnergy(energyToBuild, false);
                }
            }
    
            if(created == false )
                lastFailedAttemptsToCreateField.put(fieldPosition, level.getGameTime());
        }
        
        return created;
    }
    
    protected boolean createField(BlockPos blockPos)
    {
        boolean created = createFieldWithoutInitialization(blockPos, false);
        if(created)
            initializeField(blockPos);
        return created;
    }
    
    protected void initializeField(BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity fieldBlockEntity)
        {
            fieldBlockEntity.setProjectorPosition(worldPosition);
        }
        else
        {
            throw new IllegalStateException("FieldBlock not found in blockPos");
        }
    }
    
    public void onFieldDestroyed(FieldBlockEntity fieldBlockEntity)
    {
        if(level.isClientSide())
            return;
        boolean isMine = fieldBlockEntity.isMyProjector(this);
        boolean isDestroyed = fieldBlockEntity.isRemoved();
        
        if(isDestroyed == false && isMine)
            throw new IllegalStateException("fieldBlockEntity is not destroyed.");
        if(isDestroyed && isMine == false)
            throw new IllegalStateException("projector is not the owner of this fieldBlockEntity");
        
        BlockPos fieldPosition = fieldBlockEntity.getBlockPos();
        var state = fields.getState(fieldPosition);
        
        boolean wasSupporting = state == FieldSet.FieldState.Created || state == FieldSet.FieldState.RemovedFromShape;
        boolean isInShape = state != FieldSet.FieldState.Unknown && state != FieldSet.FieldState.RemovedFromShape;
        
        fieldsToDestroy.remove(fieldPosition);
        
        if(wasSupporting)
            totalEnergyNeededToSupport -= getEnergyToSupportField(fieldPosition);
        
        if(isInShape)
            fields.setState(fieldPosition, FieldSet.FieldState.NotCreated);
        else
            fields.remove(fieldPosition);
        
        if(state == FieldSet.FieldState.RemovedFromShape)
            stopCountingFieldForChunkLoading(fieldPosition);
        
        lastFailedAttemptsToCreateField.remove(fieldPosition);
    }
    public void onFieldCreated(FieldBlockEntity fieldBlockEntity)
    {
        if(level.isClientSide())
            return;
        if(fieldBlockEntity.isMyProjector(this) == false)
            throw new IllegalStateException("fieldBlockEntity's projector is not this one");
        
        BlockPos fieldPosition = fieldBlockEntity.getBlockPos();
        var state = fields.getState(fieldPosition);
    
        boolean isInShape = state != FieldSet.FieldState.Unknown && state != FieldSet.FieldState.RemovedFromShape;
        boolean shouldSupport = state != FieldSet.FieldState.Created && state != FieldSet.FieldState.RemovedFromShape;
    
        if(shouldSupport)
            totalEnergyNeededToSupport += getEnergyToSupportField(fieldPosition);
        
        if(isInShape)
        {
            fields.setState(fieldPosition, FieldSet.FieldState.Created);
        }
        else
        {
            fields.setState(fieldPosition, FieldSet.FieldState.RemovedFromShape);
            fieldsToDestroy.add(fieldPosition);
            countFieldForChunkLoading(fieldPosition);
        }
    
        cashedEnergyToBuild.remove(fieldPosition);
        lastFailedAttemptsToCreateField.remove(fieldPosition);
        
        fieldBlockEntity.setCamouflage(camouflageBlockState);
    }
    
    protected void addChunkLoader(ChunkPos by)
    {
        ChunkPos chunkToLoad = new ChunkPos(worldPosition);
        BlockPos blockLoader = by.getBlockAt(0, level.getMinBuildHeight(), 0);
        WorldChunksLoader.get((ServerLevel)level).forceChunk(chunkToLoad, blockLoader, worldPosition);
    }
    
    protected void removeChunkLoader(ChunkPos by)
    {
        ChunkPos chunkToLoad = new ChunkPos(worldPosition);
        BlockPos blockLoader = by.getBlockAt(0, level.getMinBuildHeight(), 0);
        WorldChunksLoader.get((ServerLevel)level).stopForcingChunk(chunkToLoad, blockLoader, worldPosition);
    }
    
    protected void removeAllChunkLoaders()
    {
        for(var entry : fieldsByChunkCounter.entrySet())
            removeChunkLoader(entry.getKey());
    }
    
    protected void countFieldForChunkLoading(BlockPos blockPos)
    {
        var chunkPos = new ChunkPos(blockPos);
        if(fieldsByChunkCounter.increase(chunkPos) == 1)
            addChunkLoader(chunkPos);
    }
    
    protected void stopCountingFieldForChunkLoading(BlockPos blockPos)
    {
        var chunkPos = new ChunkPos(blockPos);
        if(fieldsByChunkCounter.decrease(chunkPos) == 0)
            removeChunkLoader(chunkPos);
    }
    
    @Override
    public void clearFields()
    {
        if(level.isClientSide())
            return;
        fieldsToDestroy.addAll(fields.getFields(FieldSet.FieldState.Created));
        fields.changeState(FieldSet.FieldState.Created, FieldSet.FieldState.RemovedFromShape);
        
        var toClean = EnumSet.allOf(FieldSet.FieldState.class);
        toClean.remove(FieldSet.FieldState.RemovedFromShape);
        fields.clear(toClean);
        
        fieldsByChunkCounter.clear();
        fieldsToDestroy.forEach(this::countFieldForChunkLoading);
        
        cashedEnergyToBuild.clear();
        lastFailedAttemptsToCreateField.clear();
    }
    
    public void removeFields(Collection<BlockPos> positions)
    {
        if(level.isClientSide())
            return;
        for(var blockPos : positions)
            removeFieldInternal(blockPos);
    }
    
    public void retainFields(Collection<BlockPos> positions)
    {
        if(level.isClientSide())
            return;
        var iterator = fields.iteratorExcept(FieldSet.FieldState.RemovedFromShape);
        while(iterator.hasNext())
        {
            var blockPos = iterator.next();
            if(positions.contains(blockPos) == false)
                removeFieldInternal(iterator);
        }
    }
    
    @Override
    public Set<BlockPos> getAllFieldsInShape()
    {
        return fields.getAllExcept(FieldSet.FieldState.RemovedFromShape);
    }
    
    @Override
    public void removeFieldsIf(Predicate<BlockPos> predicate)
    {
        if(level.isClientSide())
            return;
        var iterator = fields.iteratorExcept(FieldSet.FieldState.RemovedFromShape);
        while(iterator.hasNext())
        {
            var blockPos = iterator.next();
            if(predicate.test(blockPos))
                removeFieldInternal(iterator);
        }
    }
    
    protected void loadCreatedFieldFromWorld(FieldBlockEntity fieldBlock)
    {
        var fieldPosition = fieldBlock.getBlockPos();
        var state = fields.getState(fieldPosition);
        
        if(state == FieldSet.FieldState.Unknown)
            countFieldForChunkLoading(fieldPosition);
        
        if(state != FieldSet.FieldState.Created)
        {
            initializeField(fieldPosition);
            onFieldCreated(fieldBlock);
        }
    }
    
    protected void loadCreatedFieldFromWorld(BlockPos blockPos)
    {
        if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity fieldBlock && fieldBlock.isMyProjector(this))
            loadCreatedFieldFromWorld(fieldBlock);
    }
    
    protected void loadCreatedFieldsFromWorldByShape()
    {
        HashMap<BlockPos, FieldBlockEntity> createdPoses = new HashMap<>();
        var iterator = fields.iterator(FieldSet.FieldState.NotCreated, FieldSet.FieldState.NotCreatedTwice, FieldSet.FieldState.NoCreatedTwiceTestingForCreation);
        while(iterator.hasNext())
        {
            var blockPos = iterator.next();
            if(level.getBlockEntity(blockPos) instanceof FieldBlockEntity blockEntity && blockEntity.isMyProjector(this))
                createdPoses.put(blockPos, blockEntity);
        }
        for(var fieldBlock : createdPoses.values())
            loadCreatedFieldFromWorld(fieldBlock);
    }
    
    @Override
    public void addField(BlockPos blockPos)
    {
        if(level.isClientSide())
            return;
        if(isImpossibleToBuildIn(blockPos))
            return;
        
        var state = fields.getState(blockPos);
        if(state == FieldSet.FieldState.RemovedFromShape)
        {
            fieldsToDestroy.remove(blockPos);
            fields.setState(blockPos, FieldSet.FieldState.Created);
        }
        else if(state == FieldSet.FieldState.Unknown)
        {
            fields.setState(blockPos, FieldSet.FieldState.NotCreated);
            loadCreatedFieldFromWorld(blockPos);
            
            countFieldForChunkLoading(blockPos);
        }
    }
    
    private void removeFieldInternal(BlockPos fieldPosition)
    {
        var state = fields.getState(fieldPosition);
        if(state == FieldSet.FieldState.Created)
        {
            fieldsToDestroy.add(fieldPosition);
            fields.setState(fieldPosition, FieldSet.FieldState.RemovedFromShape);
        }
        else if(state != FieldSet.FieldState.Unknown)
        {
            stopCountingFieldForChunkLoading(fieldPosition);
            fields.remove(fieldPosition);
        }
        cashedEnergyToBuild.remove(fieldPosition);
        lastFailedAttemptsToCreateField.remove(fieldPosition);
    }
    
    private void removeFieldInternal(FieldSet.Iterator iterator)
    {
        var state = iterator.getCurrentState();
        var fieldPosition = iterator.getCurrentBlockPos();
        if(state == FieldSet.FieldState.Created)
        {
            fieldsToDestroy.add(fieldPosition);
            iterator.setState(FieldSet.FieldState.RemovedFromShape);
        }
        else
        {
            stopCountingFieldForChunkLoading(fieldPosition);
            iterator.remove();
        }
        cashedEnergyToBuild.remove(fieldPosition);
        lastFailedAttemptsToCreateField.remove(fieldPosition);
    }
    
    @Override
    public void removeField(BlockPos blockPos)
    {
        if(level.isClientSide())
            return;
        removeFieldInternal(blockPos);
    }
    
    protected void destroyRequestedFields(int count)
    {
        for(var blockPos : fieldsToDestroy.stream().limit(count).toList())
            level.removeBlock(blockPos, false);
    }
    
    protected int consumeEnergyToSupportFields()
    {
        BetterEnergyStorage energyStorage = getEnergyStorage();
        var consumed = energyStorage.consumeEnergy(totalEnergyNeededToSupport, false);
        return Math.max(0, totalEnergyNeededToSupport - consumed);
    }
    
    protected void requestToDestroyFieldsWhichCantSupport(int energyLack)
    {
        var iterator = fields.iterator(FieldSet.FieldState.Created);
        while(iterator.hasNext() && energyLack > 0)
        {
            var blockPos = iterator.next();
            var neededEnergy = getEnergyToSupportField(blockPos);
            energyLack -= neededEnergy;
            fieldsToDestroy.add(blockPos);
        }
    }
    
    protected boolean shouldTryToCreateField(BlockPos fieldPosition)
    {
        var lastAttempt = lastFailedAttemptsToCreateField.get(fieldPosition);
    
        if(lastAttempt == null)
            return true;
        else
            return WorldChunkChanges.get((ServerLevel)level).isChunkUpdatedNotBefore(new ChunkPos(fieldPosition), lastAttempt);
    }
    
    protected void createFieldBlocks(int count)
    {
        boolean tested = false;
        var iterator = fields.iterator(FieldSet.FieldState.NotCreated);
        if(iterator.hasNext() == false)
        {
            tested = true;
            iterator = fields.iterator(FieldSet.FieldState.NoCreatedTwiceTestingForCreation);
            if(iterator.hasNext() == false)
            {
                iterator = fields.iterator(FieldSet.FieldState.NotCreatedTwice);
                while(iterator.hasNext())
                {
                    var blockPos = iterator.next();
        
                    if(shouldTryToCreateField(blockPos))
                        iterator.setState(FieldSet.FieldState.NoCreatedTwiceTestingForCreation);
                }
                
                iterator = fields.iterator(FieldSet.FieldState.NoCreatedTwiceTestingForCreation);
                
            }
        }
        
        HashSet<BlockPos> toInitialize = new HashSet<>();
        
        while(count > 0 && iterator.hasNext())
        {
            var blockPos = iterator.next();
    
            boolean created = createFieldWithoutInitialization(blockPos, tested);
    
            count--;
            if(created)
            {
                toInitialize.add(blockPos);
            }
            else
            {
                iterator.setState(FieldSet.FieldState.NotCreatedTwice);
            }
        }
        
        toInitialize.forEach(blockPos -> initializeField(blockPos));
    }
    
    protected void requestToDestroyAllCreatedFields()
    {
        var createdFields = fields.getFields(FieldSet.FieldState.Created);
        fieldsToDestroy.addAll(createdFields);
    }
    
    @Deprecated // you should not remove all field blocks instantly
    protected void destroyAllCreatedFieldsInstantly()
    {
        requestToDestroyAllCreatedFields();
        destroyRequestedFields(fieldsToDestroy.size());
    }
    
    protected void cancelDestroyingFieldInShape()
    {
        var createdFields = fields.getFields(FieldSet.FieldState.Created);
        fieldsToDestroy.removeAll(createdFields);
    }
    
    @Override
    public void onDestroying()
    {
        if(level.isClientSide() == false)
        {
            destroyAllCreatedFieldsInstantly(); // TODO: should not remove instantly
            removeAllChunkLoaders();
        }
    }
    
    @Override
    public int getEnergyToBuildField(BlockPos blockPos)
    {
        var result = cashedEnergyToBuild.get(blockPos);
        if(result != null)
            return result;
        
        double distanceToBlockSqr = worldPosition.distSqr(blockPos);
        if(distanceToBlockSqr <= 36)
        {
            result = DEFAULT_ENERGY_TO_BUILD_FIELD;
        }
        else
        {
            var distance = Math.sqrt(distanceToBlockSqr);
            result = (int)(DEFAULT_ENERGY_TO_BUILD_FIELD * (distance - 5));
        }
        cashedEnergyToBuild.put(blockPos, result);
        return result;
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
        if(level.isClientSide() == false)
            cancelDestroyingFieldInShape();
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
    public void onLoad()
    {
        super.onLoad();
        if(level.isClientSide() == false)
            loadCreatedFieldsFromWorldByShape();
    }
    
    @Override
    public void serverTick(ServerLevel level, BlockPos blockPos, BlockState blockState)
    {
        destroyRequestedFields(getMaxFieldCanDestroyPerTick());
        int energyLack = consumeEnergyToSupportFields();
    
        if(energyLack > 0)
            requestToDestroyFieldsWhichCantSupport(energyLack);
        else if(canWork())
            createFieldBlocks(getMaxFieldCanBuildPerTick());
    }
    
    @Override
    public BlockPos getPosition()
    {
        return worldPosition;
    }
}
