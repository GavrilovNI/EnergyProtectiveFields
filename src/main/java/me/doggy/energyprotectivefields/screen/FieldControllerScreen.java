package me.doggy.energyprotectivefields.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FieldControllerScreen extends BaseScreen<FieldControllerMenu>
{
    public static final int ENERGY_SCALE_START_X = 8;
    public static final int ENERGY_SCALE_START_Y = 16;
    public static final int ENERGY_SCALE_FILLER_START_X = 176;
    public static final int ENERGY_SCALE_FILLER_START_Y = 0;
    public static final int ENERGY_SCALE_WIDTH = 16;
    public static final int ENERGY_SCALE_HEIGHT = 60;
    
    public FieldControllerScreen(FieldControllerMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/field_controller_gui.png", 176, 174);
    }
    
    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY)
    {
        super.renderTooltip(pPoseStack, pX, pY);
        
        boolean isHoveringEnergy = pX >= ENERGY_SCALE_START_X + leftPos && pX < ENERGY_SCALE_START_X + ENERGY_SCALE_WIDTH + leftPos
                && pY >= ENERGY_SCALE_START_Y + topPos && pY < ENERGY_SCALE_START_Y + ENERGY_SCALE_HEIGHT + topPos;
        
        if (this.menu.getCarried().isEmpty() && isHoveringEnergy)
        {
            IEnergyStorage energyStorage = this.menu.getEnergyStorage();
            renderTooltip(pPoseStack, new TextComponent("Energy: " + energyStorage.getEnergyStored() + "/" + energyStorage.getMaxEnergyStored()), pX, pY);
        }
        
    }
    
    public float getEnergyPercent()
    {
        IEnergyStorage energyStorage = this.menu.getEnergyStorage();
        return ((float)energyStorage.getEnergyStored()) / energyStorage.getMaxEnergyStored();
    }
    
    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY)
    {
        super.renderBg(pPoseStack, pPartialTick, pMouseX, pMouseY);
        
        float energyPercent = getEnergyPercent();
        
        int heightToDraw = (int)Math.floor(energyPercent * ENERGY_SCALE_HEIGHT);
        if(heightToDraw == 0 && energyPercent > 0)
            heightToDraw = 1;
    
        int yOffset = ENERGY_SCALE_HEIGHT - heightToDraw;
        this.blit(pPoseStack, leftPos + ENERGY_SCALE_START_X, topPos + ENERGY_SCALE_START_Y + yOffset,
                ENERGY_SCALE_FILLER_START_X, ENERGY_SCALE_FILLER_START_Y + yOffset, ENERGY_SCALE_WIDTH, heightToDraw);
    }
}
