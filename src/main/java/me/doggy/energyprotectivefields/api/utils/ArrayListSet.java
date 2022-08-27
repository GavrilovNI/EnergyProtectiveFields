package me.doggy.energyprotectivefields.api.utils;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayListSet<E> extends ArrayList<E>
{
    public ArrayListSet(int initialCapacity) {
        super(initialCapacity);
    }
    
    public ArrayListSet() {
        super();
    }
    
    public ArrayListSet(Collection<? extends E> c) {
        super(c);
    }
    
    @Override
    public boolean add(E e)
    {
        if(contains(e))
            return false;
        return super.add(e);
    }
    
    @Override
    public void add(int index, E element)
    {
        if(contains(element))
            return;
        super.add(index, element);
    }
    
    @Override
    public E set(int index, E element)
    {
        if(contains(element))
            return element;
        return super.set(index, element);
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c)
    {
        boolean changed = false;
        for(var item : c)
        {
            if(add(item))
                changed = true;
        }
        return changed;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c)
    {
        boolean changed = false;
        int lastIndex = this.size();
        for(var item : c)
        {
            if(contains(item))
                continue;
            super.add(lastIndex++,  item);
            changed = true;
        }
        return changed;
    }
    
    public void swap(int indexA, int indexB)
    {
        if(indexA < 0 || indexA >= this.size())
            throw new IllegalArgumentException("indexA out of range");
        if(indexB < 0 || indexB >= this.size())
            throw new IllegalArgumentException("indexB out of range");
        
        var a = get(indexA);
        var b = get(indexB);
        set(indexA, b);
        set(indexB, a);
    }
}
