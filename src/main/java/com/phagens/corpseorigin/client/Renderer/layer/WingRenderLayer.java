package com.phagens.corpseorigin.client.Renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.model.WingModel;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * 尸化羽翼渲染层 - 渲染尸兄玩家的羽翼
 */
public class WingRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // 羽翼纹理
    public static final ResourceLocation WING_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/corpse_wing.png");

    private final WingModel model;

    public WingRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, 
                           WingModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, 
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount, 
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 只渲染尸兄玩家且拥有羽翼
        if (!PlayerCorpseData.isCorpse(player)) {
            return;
        }
        
        if (!PlayerCorpseData.hasWing(player)) {
            return;
        }

        // 检查是否伪装
        if (PlayerCorpseData.isDisguised(player)) {
            return;
        }

        // 获取玩家身体模型
        PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();

        // 复制身体姿势到羽翼模型
        model.copyFromBody(parentModel.body);

        // 设置动画
        model.setFlying(player.isFallFlying() || player.isInFluidType());
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 渲染羽翼
        poseStack.pushPose();

        // 获取渲染缓冲区
        RenderType renderType = RenderType.entityTranslucent(WING_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // 渲染模型（使用白色，让纹理正常显示）
        int color = 0xFFFFFFFF;

        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }
}