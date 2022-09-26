package me.doggy.energyprotectivefields.api.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashCounter<K>
{
    private final HashMap<K, Integer> map = new HashMap<>();
    private int defaultValue;
    
    public HashCounter()
    {
        this(0);
    }
    
    public HashCounter(int defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    public int getDefaultValue()
    {
        return defaultValue;
    }
    
    public void setDefaultValue(int defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    
    public int get(K key)
    {
        return map.getOrDefault(key, defaultValue);
    }
    
    public int set(K key, int value)
    {
        Integer previousValue;
        if(value == defaultValue)
            previousValue = map.remove(key);
        else
            previousValue = map.put(key, value);
        
        return previousValue == null ? defaultValue : previousValue;
    }
    
    public int clear(K key)
    {
        var previousValue = map.remove(key);
        return previousValue == null ? defaultValue : previousValue;
    }
    
    public void clear()
    {
        map.clear();
    }
    
    // returns new value
    public int increase(K key, int count)
    {
        if(count != 0)
        {
            var oldValue = map.getOrDefault(key, defaultValue);
            var newValue = oldValue + count;
            if(newValue == defaultValue)
                map.remove(key);
            else
                map.put(key, newValue);
            
            return newValue;
        }
        return map.getOrDefault(key, defaultValue);
    }
    
    public int getTotal()
    {
        int result = 0;
        for(var value : map.values())
            result += value;
        return result;
    }
    
    public int increase(K key)
    {
        return increase(key, 1);
    }
    
    public int decrease(K key, int count)
    {
        return increase(key, -count);
    }
    
    public int decrease(K key)
    {
        return decrease(key, 1);
    }
    
    public Set<Map.Entry<K, Integer>> entrySet()
    {
        return map.entrySet();
    }
}
