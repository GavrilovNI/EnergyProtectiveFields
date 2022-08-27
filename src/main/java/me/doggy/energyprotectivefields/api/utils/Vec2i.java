package me.doggy.energyprotectivefields.api.utils;

import com.google.common.base.MoreObjects;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

@Immutable
public class Vec2i implements Comparable<Vec2i>
{
    public static final Vec2i ZERO = new Vec2i(0, 0);
    
    private int x;
    private int y;
    
    public Vec2i(int pX, int pY) {
        this.x = pX;
        this.y = pY;
    }
    
    public Vec2i(double pX, double pY) {
        this(Mth.floor(pX), Mth.floor(pY));
    }
    
    public Vec3i toVec3i(int z)
    {
        return new Vec3i(x, y, z);
    }
    
    public Vec3i toVec3i()
    {
        return new Vec3i(x, y, 0);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if(this == o)
            return true;
        if(o == null || getClass() != o.getClass())
            return false;
        Vec2i vec2i = (Vec2i)o;
        return x == vec2i.x && y == vec2i.y;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(x, y);
    }
    
    public int compareTo(Vec2i p_123330_) {
        if (this.getY() == p_123330_.getY()) {
            return this.getX() - p_123330_.getX();
        } else {
            return this.getY() - p_123330_.getY();
        }
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    protected Vec2i setX(int pX) {
        this.x = pX;
        return this;
    }
    
    protected Vec2i setY(int pY) {
        this.y = pY;
        return this;
    }
    
    public Vec2i offset(double pDx, double pDy) {
        return pDx == 0.0D && pDy == 0.0D ? this : new Vec2i((double)this.getX() + pDx, (double)this.getY() + pDy);
    }
    
    public Vec2i offset(int pDx, int pDy) {
        return pDx == 0 && pDy == 0 ? this : new Vec2i(this.getX() + pDx, this.getY() + pDy);
    }
    
    public Vec2i offset(Vec2i pVector) {
        return this.offset(pVector.getX(), pVector.getY());
    }
    
    public Vec2i subtract(Vec2i pVec) {
        return this.offset(-pVec.getX(), -pVec.getY());
    }
    
    public Vec2i multiply(int pScalar) {
        if (pScalar == 1) {
            return this;
        } else {
            return pScalar == 0 ? ZERO : new Vec2i(this.getX() * pScalar, this.getY() * pScalar);
        }
    }
    
    public Vec2i above() {
        return this.above(1);
    }
    
    public Vec2i above(int pDistance) {
        return offset(0, pDistance);
    }
    
    public Vec2i below() {
        return this.below(1);
    }
    
    public Vec2i below(int pDistance) {
        return offset(0, -pDistance);
    }
    
    public Vec2i left() {
        return this.left(1);
    }
    
    public Vec2i left(int pDistance) {
        return offset(-pDistance, 0);
    }
    
    public Vec2i right() {
        return this.right(1);
    }
    
    public Vec2i right(int pDistance) {
        return offset(pDistance, 0);
    }
    
    public boolean closerThan(Vec2i pVector, double pDistance) {
        return this.distSqr(pVector) < Mth.square(pDistance);
    }
    
    public boolean closerToCenterThan(Position pPosition, double pDistance) {
        return this.distToCenterSqr(pPosition) < Mth.square(pDistance);
    }
    
    public double distSqr(Vec3i pVector) {
        return this.distToLowCornerSqr(pVector.getX(), pVector.getY(), pVector.getZ());
    }
    
    public double distSqr(Vec2i pVector) {
        return this.distToLowCornerSqr(pVector.getX(), pVector.getY(), 0);
    }
    
    public double distToCenterSqr(Position pPosition) {
        return this.distToCenterSqr(pPosition.x(), pPosition.y(), pPosition.z());
    }
    
    public double distToCenterSqr(double pX, double pY, double pZ) {
        double d0 = (double)this.getX() + 0.5D - pX;
        double d1 = (double)this.getY() + 0.5D - pY;
        double d2 = 0.5D - pZ;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }
    
    public double distToLowCornerSqr(double pX, double pY, double pZ) {
        double d0 = (double)this.getX() - pX;
        double d1 = (double)this.getY() - pY;
        return d0 * d0 + d1 * d1 + pZ * pZ;
    }
    
    public int distManhattan(Vec3i pVector) {
        float f = (float)Math.abs(pVector.getX() - this.getX());
        float f1 = (float)Math.abs(pVector.getY() - this.getY());
        return (int)(f + f1);
    }
    
    public int distManhattan(Vec2i pVector) {
        float f = (float)Math.abs(pVector.getX() - this.getX());
        float f1 = (float)Math.abs(pVector.getY() - this.getY());
        return (int)(f + f1);
    }
    
    public int get(Direction.Axis pAxis) {
        return pAxis.choose(this.x, this.y, 0);
    }
    
    public String toString() {
        return MoreObjects.toStringHelper(this).add("x", this.getX()).add("y", this.getY()).toString();
    }
    
    public String toShortString() {
        return this.getX() + ", " + this.getY();
    }
}