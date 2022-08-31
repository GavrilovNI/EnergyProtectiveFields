package me.doggy.energyprotectivefields.api.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class Math3D
{
    public static Vec3 getClosestPointOfBlock(Vec3i blockPos, Vec3 to)
    {
        double x, y, z;
        
        if(to.x <= blockPos.getX())
            x = blockPos.getX();
        else if(to.x >= blockPos.getX() + 1)
            x = blockPos.getX() + 1;
        else
            x = to.x;
        
        if(to.y <= blockPos.getY())
            y = blockPos.getY();
        else if(to.y >= blockPos.getY() + 1)
            y = blockPos.getY() + 1;
        else
            y = to.y;
        
        if(to.z <= blockPos.getZ())
            z = blockPos.getZ();
        else if(to.z >= blockPos.getZ() + 1)
            z = blockPos.getZ() + 1;
        else
            z = to.z;
        
        return new Vec3(x, y, z);
    }
    
    public static Vec3 getFarthestPointOfBlock(Vec3i blockPos, Vec3 to)
    {
        double x, y, z;
        
        if(to.x <= blockPos.getX())
            x = blockPos.getX() + 1;
        else if(to.x >= blockPos.getX() + 1)
            x = blockPos.getX();
        else
            x = (to.x - blockPos.getX()) > 0.5 ? blockPos.getX() : blockPos.getX() + 1;
        
        if(to.y <= blockPos.getY())
            y = blockPos.getY() + 1;
        else if(to.y >= blockPos.getY() + 1)
            y = blockPos.getY();
        else
            y = (to.y - blockPos.getY()) > 0.5 ? blockPos.getY() : blockPos.getY() + 1;
        
        if(to.z <= blockPos.getZ())
            z = blockPos.getZ() + 1;
        else if(to.z >= blockPos.getZ() + 1)
            z = blockPos.getZ();
        else
            z = (to.z - blockPos.getZ()) > 0.5 ? blockPos.getZ() : blockPos.getZ() + 1;
        
        return new Vec3(x, y, z);
    }
    
    public static BlockPos multiply(Vec3i a, Vec3i b)
    {
        return new BlockPos(a.getX() * b.getX(), a.getY() * b.getY(), a.getZ() * b.getZ());
    }
}
