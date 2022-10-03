package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class FieldSet
{
    public enum FieldState
    {
        Unknown,
        NotCreated,
        NotCreatedTwice,
        NoCreatedTwiceTestingForCreation,
        Created,
        RemovedFromShape
    }
    
    public static class Iterator implements java.util.Iterator<BlockPos>
    {
        private java.util.Iterator<BlockPos> realIterator;
        private final FieldSet fieldSet;
        private FieldState iteratingState;
        private FieldState currentState = null;
        private BlockPos currentBlockPos = null;
        
        private final Queue<FieldState> nextStates = new LinkedList<>();
        
        protected Iterator()
        {
            this.fieldSet = null;
            this.realIterator = new java.util.Iterator<BlockPos>()
            {
                @Override
                public boolean hasNext()
                {
                    return false;
                }
    
                @Override
                public BlockPos next()
                {
                    return null;
                }
            };
            this.iteratingState = null;
        }
        
        public Iterator(FieldSet fieldSet, FieldState iteratingState, FieldState ... nextStates)
        {
            this.fieldSet = fieldSet;
            if(iteratingState == FieldState.Unknown || Arrays.stream(nextStates).toList().contains(FieldState.Unknown))
                throw new IllegalArgumentException("Can't iterate by Unknown fieldState");
    
            this.realIterator = fieldSet.fields.get(iteratingState).iterator();
            this.iteratingState = iteratingState;
            
            this.nextStates.addAll(Arrays.stream(nextStates).toList());
        }
        
        public BlockPos getCurrentBlockPos()
        {
            return currentBlockPos;
        }
        
        public FieldState getCurrentState()
        {
            return currentState;
        }
        
        public FieldState getIteratingState()
        {
            return iteratingState;
        }
        
        @Nullable
        private java.util.Iterator getNextRealIterator()
        {
            FieldState nextState = nextStates.poll();
            if(nextState == null)
                return null;
            return fieldSet.fields.get(iteratingState).iterator();
        }
        
        @Override
        public boolean hasNext()
        {
            if(realIterator.hasNext())
                return true;
            
            for(var nextState : nextStates.stream().toList())
            {
                var iterator = fieldSet.fields.get(nextState).iterator();
                if(iterator.hasNext())
                    return true;
            }
            return false;
        }
    
        @Override
        public BlockPos next()
        {
            if(realIterator.hasNext() == false)
            {
                realIterator = null;
                iteratingState = null;
                
                do
                {
                    iteratingState = nextStates.poll();
                    realIterator = iteratingState == null ? null : fieldSet.fields.get(iteratingState).iterator();
                }
                while(realIterator != null && realIterator.hasNext() == false);
            }
            if(realIterator == null)
                throw new NoSuchElementException();
            
            currentBlockPos = realIterator.next();
            currentState = iteratingState;
            return currentBlockPos;
        }
    
        @Override
        public void remove()
        {
            realIterator.remove();
            currentState = FieldState.Unknown;
        }
        
        public void setState(FieldState state)
        {
            if(state == iteratingState)
            {
                if(state != currentState)
                    throw new UnsupportedOperationException("can't change iterator state to the initial one");
            }
            else
            {
                if(iteratingState == currentState)
                    remove();
                fieldSet.setState(currentBlockPos, state);
                currentState = state;
            }
        }
    
        @Override
        protected Object clone() throws CloneNotSupportedException
        {
            throw new CloneNotSupportedException();
        }
    
        @Override
        public void forEachRemaining(Consumer<? super BlockPos> action)
        {
            realIterator.forEachRemaining(action);
        }
    
        @Override
        public boolean equals(Object o)
        {
            if(this == o)
                return true;
            if(o == null || getClass() != o.getClass())
                return false;
            Iterator iterator = (Iterator)o;
            return Objects.equals(realIterator, iterator.realIterator) && Objects.equals(fieldSet,
                    iterator.fieldSet) && iteratingState == iterator.iteratingState && Objects.equals(currentBlockPos, iterator.currentBlockPos);
        }
    
        @Override
        public int hashCode()
        {
            return Objects.hash(realIterator, fieldSet, iteratingState, currentBlockPos);
        }
    }
    
    private final HashMap<FieldState, HashSet<BlockPos>> fields = new HashMap<>();
    
    public FieldSet()
    {
        var states = EnumSet.allOf(FieldState.class);
        states.remove(FieldState.Unknown);
        for(var state : states)
            fields.put(state, new HashSet<>());
    }
    
    public FieldState setState(BlockPos blockPos, FieldState fieldState)
    {
        var currentState = remove(blockPos);
        
        if(fieldState != FieldState.Unknown)
            fields.get(fieldState).add(blockPos);
        
        return currentState;
    }
    
    public FieldState remove(BlockPos blockPos)
    {
        var currentState = getState(blockPos);
        if(currentState != FieldState.Unknown)
            fields.get(currentState).remove(blockPos);
        return currentState;
    }
    
    public void retainAll(Collection<BlockPos> blockPoses)
    {
        for(var poses : fields.values())
            poses.retainAll(blockPoses);
    }
    
    public void setAll(Set<BlockPos> blockPoses, FieldState fieldState)
    {
        for(var blockPos : blockPoses)
            setState(blockPos, fieldState);
    }
    
    public boolean contains(BlockPos blockPos)
    {
        return getState(blockPos) != FieldState.Unknown;
    }
    
    public FieldState getState(BlockPos blockPos)
    {
        for(var entry : fields.entrySet())
        {
            if(entry.getValue().contains(blockPos))
                return entry.getKey();
        }
        return FieldState.Unknown;
    }
    
    public void changeState(FieldState from, FieldState to)
    {
        if(from == FieldState.Unknown)
            return;
        
        if(to == FieldState.Unknown)
        {
            clear(from);
        }
        else
        {
            var toSet = fields.get(to);
            var fromSet = fields.get(from);
            if(toSet.isEmpty())
                fields.put(to, fromSet);
            else
                toSet.addAll(fromSet);
            fields.put(from, new HashSet<>());
        }
    }
    
    public Iterator iterator()
    {
        EnumSet<FieldState> set = EnumSet.allOf(FieldState.class);
        set.remove(FieldState.Unknown);
        set.remove(FieldState.Created);
        return iterator(FieldState.Created, set.toArray(new FieldState[0]));
    }
    
    public Iterator iteratorExcept(FieldState ... others)
    {
        EnumSet<FieldState> set = EnumSet.allOf(FieldState.class);
        set.remove(FieldState.Unknown);
        set.removeAll(Arrays.stream(others).toList());
        if(set.isEmpty())
            return new Iterator();
        var setIterator = set.iterator();
        var first = setIterator.next();
        setIterator.remove();
        return iterator(first, set.toArray(new FieldState[0]));
    }
    
    public Iterator iterator(FieldState fieldState)
    {
        return new Iterator(this, fieldState);
    }
    
    public Iterator iterator(FieldState first, FieldState ... others)
    {
        return new Iterator(this, first, others);
    }
    
    public Set<BlockPos> getFields(FieldState fieldState)
    {
        return (Set<BlockPos>)fields.get(fieldState).clone();
    }
    
    public Set<BlockPos> getFields(FieldState first, FieldState ... fieldStates)
    {
        return getFields(EnumSet.of(first, fieldStates));
    }
    
    public Set<BlockPos> getFields(Set<FieldState> fieldStates)
    {
        HashSet<FieldState> realStates = new HashSet<>(fieldStates);
        realStates.remove(FieldState.Unknown);
        
        var result = new HashSet<BlockPos>();
        for(var fieldState : realStates)
            result.addAll((Collection<? extends BlockPos>)fields.get(fieldState).clone());
        return result;
    }
    
    public Set<BlockPos> getAll()
    {
        return getFields(fields.keySet());
    }
    
    public Set<BlockPos> getAllExcept(FieldState ... except)
    {
        return getAllExcept(Arrays.stream(except).collect(Collectors.toSet()));
    }
    
    public Set<BlockPos> getAllExcept(Collection<FieldState> except)
    {
        var states = new HashSet<>(fields.keySet());
        states.removeAll(except);
        return getFields(states);
    }
    
    public void forEach(BiConsumer<BlockPos, FieldState> action)
    {
        for(var entry : fields.entrySet())
        {
            var state = entry.getKey();
            entry.getValue().forEach(pos -> action.accept(pos, state));
        }
    }
    
    public void clear()
    {
        for(var state : fields.keySet())
            clear(state);
    }
    
    public void clear(FieldState fieldState)
    {
        if(fieldState != FieldState.Unknown)
            fields.put(fieldState, new HashSet<>());
    }
    
    public void clear(Set<FieldState> fieldStates)
    {
        for(var state : fieldStates)
            clear(state);
    }
}
