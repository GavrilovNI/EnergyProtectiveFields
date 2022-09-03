package me.doggy.energyprotectivefields.api;

import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;

public interface IFieldStateListener
{
    void onFieldDestroyed(FieldBlockEntity fieldBlockEntity);
    void onFieldCreated(FieldBlockEntity fieldBlockEntity);
}
