package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.function.Predicate;

public interface IFieldsContainer
{
    IFieldsContainer EMPTY = new IFieldsContainer()
    {
        @Override
        public void add(BlockPos blockPos)
        {
        
        }
    
        @Override
        public void remove(BlockPos blockPos)
        {
        
        }
    
        @Override
        public void clear()
        {
        
        }
    
        @Override
        public void addAll(Collection<BlockPos> positions)
        {
        
        }
    
        @Override
        public void removeAll(Collection<BlockPos> positions)
        {
        
        }
    
        @Override
        public void retainAll(Collection<BlockPos> positions)
        {
        
        }
    
        @Override
        public void removeIf(Predicate<BlockPos> predicate)
        {
        
        }
    };
    
    void add(BlockPos blockPos);
    void remove(BlockPos blockPos);
    
    void clear();
    
    void addAll(Collection<BlockPos> positions);
    void removeAll(Collection<BlockPos> positions);
    void retainAll(Collection<BlockPos> positions);
    void removeIf(Predicate<BlockPos> predicate);
}
