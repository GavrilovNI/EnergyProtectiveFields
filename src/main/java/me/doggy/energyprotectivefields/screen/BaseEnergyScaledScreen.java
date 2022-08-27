package me.doggy.energyprotectivefields.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.energy.IEnergyStorage;

public class BaseEnergyScaledScreen<T extends AbstractContainerWithEnergyMenu> extends BaseScreen<T>
{
    protected int energyScaleStartX = 8;
    protected int energyScaleStartY = 16;
    protected int energyScaleFillerStartX = 176;
    protected int energyScaleFillerStartY = 0;
    protected int energyScaleWidth = 16;
    protected int energyScaleHeight = 60;
    
    public BaseEnergyScaledScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, String textureLocation, int imageWidth, int imageHeight)
    {
        super(pMenu, pPlayerInventory, pTitle, textureLocation, imageWidth, imageHeight);
        energyScaleFillerStartX = imageWidth;
    }
    
    @Override
    protected void renderTooltip(PoseStack pPoseStack, int pX, int pY)
    {
        super.renderTooltip(pPoseStack, pX, pY);
        
        boolean isHoveringEnergy = pX >= energyScaleStartX + leftPos && pX < energyScaleStartX + energyScaleWidth + leftPos
                && pY >= energyScaleStartY + topPos && pY < energyScaleStartY + energyScaleHeight + topPos;
        
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
        
        int heightToDraw = (int)Math.floor(energyPercent * energyScaleHeight);
        if(heightToDraw == 0 && energyPercent > 0)
            heightToDraw = 1;
        
        int yOffset = energyScaleHeight - heightToDraw;
        this.blit(pPoseStack, leftPos + energyScaleStartX, topPos + energyScaleStartY + yOffset,
                energyScaleFillerStartX, energyScaleFillerStartY + yOffset, energyScaleWidth, heightToDraw);
    }
}
