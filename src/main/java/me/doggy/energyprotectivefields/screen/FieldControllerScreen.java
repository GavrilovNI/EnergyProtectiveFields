package me.doggy.energyprotectivefields.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FieldControllerScreen extends BaseEnergyScaledScreen<FieldControllerMenu>
{
    public FieldControllerScreen(FieldControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/field_controller_gui.png", 176, 183);
        energyScaleHeight = 70;
    }
}
