package me.doggy.energyprotectivefields.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class FieldProjectorScreen extends BaseEnergyScaledScreen<FieldProjectorMenu>
{
    public FieldProjectorScreen(FieldProjectorMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/field_projector_gui.png", 176, 174);
    }
}
