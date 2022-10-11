package me.doggy.energyprotectivefields.utils;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.Map;

public class PerformanceTester
{
    private static final Logger FAKE_LOGGER = new Logger()
    {
        @Override
        public String getName()
        {
            return null;
        }
    
        @Override
        public boolean isTraceEnabled()
        {
            return false;
        }
    
        @Override
        public void trace(String msg)
        {
        
        }
    
        @Override
        public void trace(String format, Object arg)
        {
        
        }
    
        @Override
        public void trace(String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void trace(String format, Object... arguments)
        {
        
        }
    
        @Override
        public void trace(String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isTraceEnabled(Marker marker)
        {
            return false;
        }
    
        @Override
        public void trace(Marker marker, String msg)
        {
        
        }
    
        @Override
        public void trace(Marker marker, String format, Object arg)
        {
        
        }
    
        @Override
        public void trace(Marker marker, String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void trace(Marker marker, String format, Object... argArray)
        {
        
        }
    
        @Override
        public void trace(Marker marker, String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isDebugEnabled()
        {
            return false;
        }
    
        @Override
        public void debug(String msg)
        {
        
        }
    
        @Override
        public void debug(String format, Object arg)
        {
        
        }
    
        @Override
        public void debug(String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void debug(String format, Object... arguments)
        {
        
        }
    
        @Override
        public void debug(String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isDebugEnabled(Marker marker)
        {
            return false;
        }
    
        @Override
        public void debug(Marker marker, String msg)
        {
        
        }
    
        @Override
        public void debug(Marker marker, String format, Object arg)
        {
        
        }
    
        @Override
        public void debug(Marker marker, String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void debug(Marker marker, String format, Object... arguments)
        {
        
        }
    
        @Override
        public void debug(Marker marker, String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isInfoEnabled()
        {
            return false;
        }
    
        @Override
        public void info(String msg)
        {
        
        }
    
        @Override
        public void info(String format, Object arg)
        {
        
        }
    
        @Override
        public void info(String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void info(String format, Object... arguments)
        {
        
        }
    
        @Override
        public void info(String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isInfoEnabled(Marker marker)
        {
            return false;
        }
    
        @Override
        public void info(Marker marker, String msg)
        {
        
        }
    
        @Override
        public void info(Marker marker, String format, Object arg)
        {
        
        }
    
        @Override
        public void info(Marker marker, String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void info(Marker marker, String format, Object... arguments)
        {
        
        }
    
        @Override
        public void info(Marker marker, String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isWarnEnabled()
        {
            return false;
        }
    
        @Override
        public void warn(String msg)
        {
        
        }
    
        @Override
        public void warn(String format, Object arg)
        {
        
        }
    
        @Override
        public void warn(String format, Object... arguments)
        {
        
        }
    
        @Override
        public void warn(String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void warn(String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isWarnEnabled(Marker marker)
        {
            return false;
        }
    
        @Override
        public void warn(Marker marker, String msg)
        {
        
        }
    
        @Override
        public void warn(Marker marker, String format, Object arg)
        {
        
        }
    
        @Override
        public void warn(Marker marker, String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void warn(Marker marker, String format, Object... arguments)
        {
        
        }
    
        @Override
        public void warn(Marker marker, String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isErrorEnabled()
        {
            return false;
        }
    
        @Override
        public void error(String msg)
        {
        
        }
    
        @Override
        public void error(String format, Object arg)
        {
        
        }
    
        @Override
        public void error(String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void error(String format, Object... arguments)
        {
        
        }
    
        @Override
        public void error(String msg, Throwable t)
        {
        
        }
    
        @Override
        public boolean isErrorEnabled(Marker marker)
        {
            return false;
        }
    
        @Override
        public void error(Marker marker, String msg)
        {
        
        }
    
        @Override
        public void error(Marker marker, String format, Object arg)
        {
        
        }
    
        @Override
        public void error(Marker marker, String format, Object arg1, Object arg2)
        {
        
        }
    
        @Override
        public void error(Marker marker, String format, Object... arguments)
        {
        
        }
    
        @Override
        public void error(Marker marker, String msg, Throwable t)
        {
        
        }
    };
    
    private final HashMap<String, Instant> timers;
    private Logger logger;
    private TemporalUnit temporalUnit;
    
    private PerformanceTester(Map<String, Instant> timers, @Nullable Logger logger, TemporalUnit temporalUnit)
    {
        this.timers = new HashMap<>(timers);
        setLogger(logger);
        this.temporalUnit = temporalUnit;
    }
    
    public PerformanceTester(@Nullable Logger logger, TemporalUnit temporalUnit)
    {
        this(new HashMap<>(), logger, temporalUnit);
    }
    
    public PerformanceTester(@Nullable Logger logger)
    {
        this(new HashMap<>(), logger, ChronoUnit.MILLIS);
    }
    
    public PerformanceTester()
    {
        this(null);
    }
    
    public String durationToString(Duration duration)
    {
        long value = duration.toNanos() / temporalUnit.getDuration().toNanos();
        return String.valueOf(value) + " " + temporalUnit.toString();
    }
    
    public PerformanceTester copy()
    {
        return new PerformanceTester(timers, logger, temporalUnit);
    }
    
    public PerformanceTester setLogger(@Nullable Logger logger)
    {
        if(logger == null)
            this.logger = FAKE_LOGGER;
        else
            this.logger = logger;
        return this;
    }
    
    public PerformanceTester setTemporalUnit(TemporalUnit temporalUnit)
    {
        this.temporalUnit = temporalUnit;
        return this;
    }
    
    public void startSilence(String name)
    {
        if(timers.put(name, Instant.now()) != null)
            throw new IllegalStateException("Timer '" + name + "' already started!");
    }
    
    public void start(String name)
    {
        logger.debug("Timer '" + name + "' has started");
        startSilence(name);
    }
    
    @Nullable
    public Duration startOrRestartSilence(String name)
    {
        if(timers.containsKey(name))
            return restartSilence(name);
        else
            startSilence(name);
        return null;
    }
    
    @Nullable
    public Duration startOrRestart(String name)
    {
        if(timers.containsKey(name))
            return restart(name);
        else
            start(name);
        return null;
    }
    
    public Duration restartSilence(String name)
    {
        var duration = stopSilence(name);
        startSilence(name);
        return duration;
    }
    
    public Duration restart(String name)
    {
        var duration = stop(name);
        start(name);
        return duration;
    }
    
    public Duration stop(String name)
    {
        var duration = stopSilence(name);
        if(duration == null)
            throw new IllegalStateException("Timer '" + name + "' was not started!");
        
        logger.debug("Timer '" + name + "' stopped with time: " + durationToString(duration));
        
        return duration;
    }
    
    @Nullable
    public Duration stopSilence(String name)
    {
        var timeStart = timers.remove(name);
        var timeEnd = Instant.now();
        if(timeStart == null)
            return null;
    
        return Duration.between(timeStart, timeEnd);
    }
    
    public Duration logNow(String name, String pointName)
    {
        var duration = get(name);
    
        logger.debug("Timer '" + name + "' point '" + pointName + "' time: " + durationToString(duration));
        return duration;
    }
    
    public Duration get(String name)
    {
        var timeStart = timers.get(name);
        var timeEnd = Instant.now();
        if(timeStart == null)
            throw new IllegalStateException("Timer '" + name + "' was not started!");
        return Duration.between(timeStart, timeEnd);
    }
    
    public boolean has(String name)
    {
        return timers.containsKey(name);
    }
    
    public void stopAll()
    {
        for(var timerName : timers.keySet())
            stop(timerName);
    }
    
    public void clear()
    {
        timers.clear();
    }
}
