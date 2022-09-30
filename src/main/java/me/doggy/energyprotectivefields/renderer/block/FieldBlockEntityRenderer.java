package me.doggy.energyprotectivefields.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.Random;

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
        BlockRenderDispatcher dispatcher = context.getBlockRenderDispatcher();
        BlockState blockState = pBlockEntity.getCamouflage();
    
        var vertexConsumer = pBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false));
        
        dispatcher.renderBatched(blockState, pBlockEntity.getBlockPos(), pBlockEntity.getLevel(), pPoseStack, vertexConsumer, true, new Random(), EmptyModelData.INSTANCE);
        //dispatcher.renderSingleBlock(blockState, pPoseStack, pBufferSource, pPackedLight, pPackedOverlay, EmptyModelData.INSTANCE);
    }
    
    @Override
    public boolean shouldRender(FieldBlockEntity pBlockEntity, Vec3 pCameraPos)
    {
        if(pBlockEntity.getBlockState().getValue(FieldBlock.RENDERING_ITSELF))
            return false;
        else
            return Vec3.atCenterOf(pBlockEntity.getBlockPos()).closerThan(pCameraPos, (double)this.getViewDistance());
    }
    
    @Override
    public int getViewDistance()
    {
        return (int)Math.ceil(Minecraft.getInstance().gameRenderer.getRenderDistance());
    }
}
