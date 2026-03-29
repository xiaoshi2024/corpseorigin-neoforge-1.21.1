package com.phagens.corpseorigin.client.Renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.model.MultiEyeModel;
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
 * 多眼渲染层 - 为尸兄玩家渲染额外的眼睛
 * 像盔甲层一样绑定到Head骨骼
 * 进化后显示全部9个眼睛
 */
public class MultiEyeRenderLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    // 多眼纹理
    public static final ResourceLocation MULTI_EYE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/multi_eye.png");

    private final MultiEyeModel model;

    public MultiEyeRenderLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                               MultiEyeModel model) {
        super(parent);
        this.model = model;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        // 只渲染尸兄玩家，且不在伪装状态
        if (!PlayerCorpseData.isCorpse(player) || PlayerCorpseData.isDisguised(player)) {
            return;
        }

        // 检查是否进化出多眼形态
        boolean hasMultiEye = PlayerCorpseData.hasMultiEye(player);
        if (!hasMultiEye) {
            return;
        }

        // 设置多眼形态
        model.setHasMultiEye(true);

        // 获取玩家模型
        PlayerModel<AbstractClientPlayer> parentModel = this.getParentModel();

        // 像盔甲层一样，直接渲染在Head位置
        poseStack.pushPose();

        // 应用Head的变换 - 像盔甲层一样
        parentModel.head.translateAndRotate(poseStack);

        // 设置动画
        model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 获取渲染缓冲区
        RenderType renderType = RenderType.entityTranslucent(MULTI_EYE_TEXTURE);
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        // 渲染模型（使用白色，让纹理正常显示）
        int color = 0xFFFFFFFF;

        model.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
    }
}
