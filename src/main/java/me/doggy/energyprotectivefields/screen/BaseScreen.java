package me.doggy.energyprotectivefields.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class BaseScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    public final ResourceLocation textureLocation;
    
    public BaseScreen(T pMenu, Inventory pPlayerInventory, Component pTitle, String textureLocation, int imageWidth, int imageHeight)
    {
        super(pMenu, pPlayerInventory, pTitle);
        this.textureLocation = new ResourceLocation(EnergyProtectiveFields.MOD_ID, textureLocation);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.inventoryLabelY = this.imageHeight - 94;
    }
    
    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f,1f,1f,1f);
        RenderSystem.setShaderTexture(0, textureLocation);
        
        this.blit(pPoseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
    
    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
    }
}
