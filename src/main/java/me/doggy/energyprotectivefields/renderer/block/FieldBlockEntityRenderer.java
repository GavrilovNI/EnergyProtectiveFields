package me.doggy.energyprotectivefields.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.doggy.energyprotectivefields.EnergyProtectiveFields;
import me.doggy.energyprotectivefields.block.FieldBlock;
import me.doggy.energyprotectivefields.block.entity.FieldBlockEntity;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class FieldBlockEntityRenderer implements BlockEntityRenderer<FieldBlockEntity>
{
    private final BlockEntityRendererProvider.Context context;
    
    public FieldBlockEntityRenderer(BlockEntityRendererProvider.Context context)
    {
        this.context = context;
    }
    
    protected boolean renderBatched(BlockRenderDispatcher dispatcher, BlockState blockStateToRender, BlockState realBlockState, BlockPos pPos, BlockAndTintGetter pLevel, PoseStack pPoseStack, VertexConsumer pConsumer, boolean pCheckSides, Random pRandom, IModelData modelData)
    {
        try {
            RenderShape rendershape = blockStateToRender.getRenderShape();
            return rendershape != RenderShape.MODEL ? false : dispatcher.getModelRenderer().tesselateBlock(pLevel, dispatcher.getBlockModel(blockStateToRender), realBlockState, pPos, pPoseStack, pConsumer, pCheckSides, pRandom, blockStateToRender.getSeed(pPos), OverlayTexture.NO_OVERLAY, modelData);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.forThrowable(throwable, "Custom tesselating block in world. By " + EnergyProtectiveFields.MOD_NAME);
            CrashReportCategory crashreportcategory = crashreport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashreportcategory, pLevel, pPos, blockStateToRender);
            throw new ReportedException(crashreport);
        }
    }
    
    @Override
    public void render(FieldBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay)
    {
        BlockRenderDispatcher dispatcher = context.getBlockRenderDispatcher();
        BlockState blockStateToRender = pBlockEntity.getCamouflage();
    
        var vertexConsumer = pBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockStateToRender, false));
    
        var level = pBlockEntity.getLevel();
        var blockPos = pBlockEntity.getBlockPos();
    
        renderBatched(dispatcher, blockStateToRender, pBlockEntity.getBlockState(), blockPos, level, pPoseStack, vertexConsumer, true, new Random(), EmptyModelData.INSTANCE);
    }
    
    @Override
    public boolean shouldRender(FieldBlockEntity pBlockEntity, @NotNull Vec3 pCameraPos)
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
