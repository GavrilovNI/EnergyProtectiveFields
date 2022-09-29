package me.doggy.energyprotectivefields.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CamouflageModuleScreen extends BaseScreen<CamouflageModuleMenu>
{
    public CamouflageModuleScreen(CamouflageModuleMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/camouflage_module_gui.png", 176, 168);
    }
}
