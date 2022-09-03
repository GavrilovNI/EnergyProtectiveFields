package me.doggy.energyprotectivefields.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class InfinityEnergyGeneratorScreen extends BaseScreen<InfinityEnergyGeneratorMenu>
{
    private EditBox maxExtractEditBox;
    
    public InfinityEnergyGeneratorScreen(InfinityEnergyGeneratorMenu pMenu, Inventory pPlayerInventory, Component pTitle)
    {
        super(pMenu, pPlayerInventory, pTitle, "textures/gui/infinity_energy_generator_gui.png", 176, 166);
        
    }
    
    @Override
    protected void init()
    {
        super.init();
        maxExtractEditBox = new EditBox(this.font, this.leftPos + this.titleLabelX, this.topPos + this.titleLabelY + 40, 100, 20, TextComponent.EMPTY);
        maxExtractEditBox.setFilter((String value) -> {
            if(value.isEmpty())
                return true;
            try {
                int result = Integer.parseInt(value);
                if(result < 0)
                    return false;
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.addRenderableWidget(maxExtractEditBox);
        var setButton = new Button(maxExtractEditBox.x + maxExtractEditBox.getWidth() + 2,
                maxExtractEditBox.y, maxExtractEditBox.getHeight(), maxExtractEditBox.getHeight(), new TextComponent("set"),
                (button) -> {
                    String valueString = maxExtractEditBox.getValue();
                    int maxExtract = 0;
                    if(valueString.isEmpty() == false)
                        maxExtract = Integer.parseInt(maxExtractEditBox.getValue());
            
                    this.menu.setMaxEnergyExtract(maxExtract);
                });
        this.addRenderableWidget(setButton);
        this.addRenderableWidget(new Button(setButton.x + setButton.getWidth() + 2,
                setButton.y, setButton.getWidth(), setButton.getHeight(), new TextComponent("max"),
                (button) -> {
                    this.menu.setMaxEnergyExtract(Integer.MAX_VALUE);
                }));
    }
    
    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
        this.font.draw(pPoseStack, "Max energy extract: " + this.menu.getMaxEnergyExtract(),
                this.titleLabelX, this.titleLabelY + 15, 0x404040);
    }
}
