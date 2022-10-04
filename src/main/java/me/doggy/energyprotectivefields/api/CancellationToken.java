package me.doggy.energyprotectivefields.api;

import java.util.concurrent.CancellationException;

public class CancellationToken
{
    public static CancellationToken CANCELLED = new CancellationToken(true);
    
    private volatile boolean cancelled;
    
    private CancellationToken(boolean cancelled)
    {
        this.cancelled = cancelled;
    }
    
    public CancellationToken()
    {
        this(false);
    }
    
    public void setCancelled()
    {
        cancelled = true;
    }
    
    public boolean isCancelled()
    {
        return cancelled;
    }
    
    public void testCancellation()
    {
        if(cancelled)
            throw new CancellationException();
    }
}
