package com.phagens.corpseorigin.client.Renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.model.TailModel;
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
 * 尸化鱼尾渲染层 - 渲染尸兄玩家的鱼尾
 */
public class TailRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // 鱼尾纹理
    public static final ResourceLocation TAIL_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/corpse_tail.png");

    private final TailModel model;

    public TailRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent, 
                           TailModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, 
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount, 
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 只渲染尸兄玩家且拥有鱼尾
        if (!PlayerCorpseData.isCorpse(player) || !PlayerCorpseData.hasTail(player)) {
            return;
        }

        // 检查是否伪装
        if (PlayerCorpseData.isDisguised(player)) {
            return;
        }

        // 获取玩家身体模型
        PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();

        // 复制身体姿势到鱼尾模型
        model.copyFromBody(parentModel.body);

        // 设置动画
        model.setSwimming(player.isInFluidType());
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 渲染鱼尾
        poseStack.pushPose();

        // 获取渲染缓冲区
        RenderType renderType = RenderType.entityTranslucent(TAIL_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // 渲染模型（使用白色，让纹理正常显示）
        int color = 0xFFFFFFFF;

        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }
}