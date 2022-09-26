package me.doggy.energyprotectivefields.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ChunkPos;

public class ChunkLoadingTesterScreen extends BaseScreen<ChunkLoadingTesterMenu>
{
    private EditBox chunkXEditBox;
    private EditBox chunkZEditBox;
    private EditBox radiusEditBox;
    
    public ChunkLoadingTesterScreen(ChunkLoadingTesterMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/chunk_loading_tester_gui.png", 176, 182);
        
    }
    
    private static boolean isInteger(String value)
    {
        if(value.isEmpty())
            return true;
        try {
            if(value.equals("-"))
                return true;
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private static boolean isNonNegativeInteger(String value)
    {
        if(value.isEmpty())
            return true;
        try {
            if(Integer.parseInt(value) < 0)
                return false;
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    @Override
    protected void init()
    {
        super.init();
    
        ChunkPos centerChunk = menu.getCenterChunk();
        int radius = menu.getRadius();
        
        int widgetsHeight = 20;
        int gap = 2;
        
        chunkXEditBox = new EditBox(this.font, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY + 25, 50, widgetsHeight, TextComponent.EMPTY);
        chunkZEditBox = new EditBox(this.font, chunkXEditBox.x + chunkXEditBox.getWidth() + gap, chunkXEditBox.y, chunkXEditBox.getWidth(), widgetsHeight, TextComponent.EMPTY);
    
        var setChunkButton = new Button(chunkZEditBox.x + chunkZEditBox.getWidth() + gap,chunkZEditBox.y,
                chunkZEditBox.getWidth(), widgetsHeight, new TextComponent("set"),
                (button) -> {
                
                    String xString = chunkXEditBox.getValue();
                    String zString = chunkZEditBox.getValue();
    
                    try {
                        int x = Integer.parseInt(xString);
                        int z = Integer.parseInt(zString);
                        menu.setCenterChunk(new ChunkPos(x, z));
                    } catch (NumberFormatException e) {
                    }
                });
        
        radiusEditBox = new EditBox(this.font, chunkXEditBox.x, chunkXEditBox.y + widgetsHeight + 15, chunkZEditBox.x + chunkZEditBox.getWidth() - chunkXEditBox.x, widgetsHeight, TextComponent.EMPTY);
    
        var setRadiusButton = new Button(radiusEditBox.x + radiusEditBox.getWidth() + gap, radiusEditBox.y,
                chunkZEditBox.getWidth(), widgetsHeight, new TextComponent("set"),
                (button) -> {
                    String radiusString = radiusEditBox.getValue();
                    if(radiusString.isEmpty() == false)
                        menu.setRadius(Integer.parseInt(radiusString));
                });
        
        chunkXEditBox.setFilter(ChunkLoadingTesterScreen::isInteger);
        chunkZEditBox.setFilter(ChunkLoadingTesterScreen::isInteger);
        radiusEditBox.setFilter(ChunkLoadingTesterScreen::isNonNegativeInteger);
        
        if(centerChunk != null)
        {
            chunkXEditBox.setValue(String.valueOf(centerChunk.x));
            chunkZEditBox.setValue(String.valueOf(centerChunk.z));
        }
        radiusEditBox.setValue(String.valueOf(radius));
    
        var resetButton = new Button(setRadiusButton.x,
                chunkXEditBox.y - widgetsHeight - gap*2, setRadiusButton.getWidth(), widgetsHeight, new TextComponent("reset"),
                (button) -> {
                    menu.resetSettings();
                });
        
        this.addRenderableWidget(chunkXEditBox);
        this.addRenderableWidget(chunkZEditBox);
        this.addRenderableWidget(radiusEditBox);
        this.addRenderableWidget(setChunkButton);
        this.addRenderableWidget(setRadiusButton);
        this.addRenderableWidget(resetButton);
    }
    
    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        var centerChunk = menu.getCenterChunk();
        this.font.draw(pPoseStack, "Center: " + (centerChunk == null ? "" : centerChunk.toString()),
                this.titleLabelX, this.titleLabelY + 15, 0x404040);
        this.font.draw(pPoseStack, "Radius: " + menu.getRadius(),
                this.titleLabelX, this.titleLabelY + 50, 0x404040);
    }
}
