package me.doggy.energyprotectivefields.api;

import net.minecraft.network.FriendlyByteBuf;

public interface INetSerializable
{
    void serializeNet(FriendlyByteBuf buf);
    
    void deserializeNet(FriendlyByteBuf buf);
}
