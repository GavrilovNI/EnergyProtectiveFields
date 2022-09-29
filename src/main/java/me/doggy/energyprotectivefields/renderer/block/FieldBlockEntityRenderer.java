package me.doggy.energyprotectivefields.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

public class FieldBlockEntityRenderer implements BlockEntityRenderer<FieldBlockEntity>
{
    private final BlockEntityRendererProvider.Context context;
    
    public FieldBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.context = context;
    }
    
    @Override
    public void render(FieldBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
    {
        if(pBlockEntity.getBlockState().getValue(FieldBlock.RENDERING_ITSELF) == false)
        {
            BlockRenderDispatcher dispatcher = context.getBlockRenderDispatcher();
            BlockState blockState = pBlockEntity.getCamouflage();
            dispatcher.renderSingleBlock(blockState, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, EmptyModelData.INSTANCE);
        }
    }
}
