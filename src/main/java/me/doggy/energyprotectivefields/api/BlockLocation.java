package me.doggy.energyprotectivefields.api;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;


public class BlockLocation extends BlockPos implements INBTSerializable<CompoundTag>
{
    private ResourceLocation levelRegistryName;
    
    private BlockLocation()
    {
        super(0, 0, 0);
        this.levelRegistryName = null;
    }
    
    public BlockLocation(ResourceLocation levelRegistryName, int x, int y, int z)
    {
        super(x, y, z);
        this.levelRegistryName = levelRegistryName;
    }
    
    public BlockLocation(Level level, int x, int y, int z)
    {
        this(level.dimension().getRegistryName(), x, y, z);
    }
    
    public BlockLocation(Level level, BlockPos blockPos)
    {
        this(level, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }
    
    public static BlockLocation fromNBT(CompoundTag nbt)
    {
        BlockLocation blockLocation = new BlockLocation();
        blockLocation.deserializeNBT(nbt);
        return blockLocation;
    }
    
    public ResourceLocation getLevelRegistryName()
    {
        return levelRegistryName;
    }
    
    @Override
    public CompoundTag serializeNBT()
    {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("position", NbtUtils.writeBlockPos(this));
        compoundTag.putString("level", levelRegistryName.toString());
        return compoundTag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag nbt)
    {
        var blockPos = NbtUtils.readBlockPos(nbt.getCompound("position"));
        this.setX(blockPos.getX());
        this.setY(blockPos.getY());
        this.setZ(blockPos.getZ());
        this.levelRegistryName = ResourceLocation.tryParse(nbt.getString("level"));
    }
}
