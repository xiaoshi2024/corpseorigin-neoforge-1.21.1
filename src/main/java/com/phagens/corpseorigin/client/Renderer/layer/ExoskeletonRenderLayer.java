package com.phagens.corpseorigin.client.Renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.model.ExoskeletonModel;
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
 * 尸化外骨骼渲染层 - 渲染 shieye 眼部和触须
 */
public class ExoskeletonRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // 外骨骼纹理
    public static final ResourceLocation EXOSKELETON_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_eye.png");

    private final ExoskeletonModel model;

    public ExoskeletonRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                                  ExoskeletonModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 只渲染尸兄玩家
        if (!PlayerCorpseData.isCorpse(player)) {
            return;
        }

        // 获取玩家头部模型
        PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();

        // 复制头部姿势到外骨骼模型
        model.copyFromHead(parentModel.head);

        // 设置动画
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 渲染外骨骼
        poseStack.pushPose();

        // 获取渲染缓冲区
        RenderType renderType = RenderType.entityTranslucent(EXOSKELETON_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // 渲染模型（使用白色，让纹理正常显示）
        int color = 0xFFFFFFFF;

        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }
}
