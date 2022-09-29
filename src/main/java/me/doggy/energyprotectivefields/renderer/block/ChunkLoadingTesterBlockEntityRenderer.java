package me.doggy.energyprotectivefields.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import me.doggy.energyprotectivefields.block.entity.ChunkLoadingTesterBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.LightLayer;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ChunkLoadingTesterBlockEntityRenderer implements BlockEntityRenderer<ChunkLoadingTesterBlockEntity>
{
    private final BlockEntityRendererProvider.Context context;
    
    public ChunkLoadingTesterBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.context = context;
    }
    
    @Override
    public void render(ChunkLoadingTesterBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
    {
        BlockRenderDispatcher dispatcher = context.getBlockRenderDispatcher();
        var lightAbove = pBlockEntity.getLevel().getBrightness(LightLayer.SKY, pBlockEntity.getBlockPos().above());
        var lightBelow = pBlockEntity.getLevel().getBrightness(LightLayer.SKY, pBlockEntity.getBlockPos().above());
        var light = Math.max(lightAbove, lightBelow);
        var brightness = 15;
        dispatcher.renderSingleBlock(pBlockEntity.getRenderingState(), pPoseStack, pBufferSource, (light << 20) + (brightness << 4), pPackedOverlay,
                EmptyModelData.INSTANCE);
    }
}
