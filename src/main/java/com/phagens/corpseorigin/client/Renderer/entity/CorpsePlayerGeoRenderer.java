//package com.phagens.corpseorigin.client.Renderer.entity;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Axis;
//import com.phagens.corpseorigin.CorpseOrigin;
//import com.phagens.corpseorigin.player.PlayerCorpseData;
//import net.minecraft.client.model.PlayerModel;
//import net.minecraft.client.model.geom.ModelPart;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//import net.minecraft.client.renderer.entity.layers.RenderLayer;
//import net.minecraft.resources.ResourceLocation;
//
///**
// * 尸族玩家渲染器 - 使用原版玩家模型 + 外骨骼纹理叠加
// */
//public class CorpsePlayerGeoRenderer extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
//
//    // 尸化骨骼覆盖纹理
//    private static final ResourceLocation SKELETON_OVERLAY =
//            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_render.png");
//    private static final ResourceLocation CRACKED_SKELETON_OVERLAY =
//            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_rendering.png");
//
//    public CorpsePlayerGeoRenderer(EntityRendererProvider.Context context) {
//        super(context, new PlayerModel<>(context.bakeLayer(net.minecraft.client.model.geom.ModelLayers.PLAYER), false), 0.5f);
//
//        // 添加外骨骼层
//        this.addLayer(new CorpseSkeletonLayer(this));
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(AbstractClientPlayer player) {
//        // 返回玩家皮肤
//        return player.getSkin().texture();
//    }
//
//    @Override
//    protected void scale(AbstractClientPlayer player, PoseStack poseStack, float partialTickTime) {
//        // 可以在这里调整模型大小
//        float scaleFactor = 0.9375F; // 原版玩家缩放
//        poseStack.scale(scaleFactor, scaleFactor, scaleFactor);
//    }
//
//    @Override
//    protected void setupRotations(AbstractClientPlayer player, PoseStack poseStack, float ageInTicks,
//                                  float rotationYaw, float partialTickTime, float scale) {
//        // 原版旋转逻辑
//        super.setupRotations(player, poseStack, ageInTicks, rotationYaw, partialTickTime, scale);
//
//        // 可以在这里添加额外的旋转调整
//    }
//
//    @Override
//    public void render(AbstractClientPlayer player, float entityYaw, float partialTickTime,
//                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//
//        // 设置模型可见性
//        this.model.setAllVisible(true);
//
//        // 调整模型姿势（使用原版玩家动画）
//        this.model.young = player.isBaby();
//
//        // 调用父类渲染
//        super.render(player, entityYaw, partialTickTime, poseStack, bufferSource, packedLight);
//    }
//
//    /**
//     * 尸化骨骼层 - 在玩家模型上叠加外骨骼纹理
//     */
//    private class CorpseSkeletonLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
//
//        public CorpseSkeletonLayer(CorpsePlayerGeoRenderer renderer) {
//            super(renderer);
//        }
//
//        @Override
//        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
//                           AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
//                           float partialTickTime, float ageInTicks, float netHeadYaw, float headPitch) {
//
//            // 获取玩家变种
//            int variant = PlayerCorpseData.getVariant(player);
//
//            // 根据变种选择纹理
//            ResourceLocation overlayTexture = variant == 1 ? CRACKED_SKELETON_OVERLAY : SKELETON_OVERLAY;
//
//            // 获取模型
//            PlayerModel<AbstractClientPlayer> model = this.getParentModel();
//
//            // 复制当前模型姿势
//            model.copyPropertiesTo(model);
//
//            // 设置渲染类型（半透明）
//            RenderType renderType = RenderType.entityTranslucent(overlayTexture);
//            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
//
//            // 渲染模型，80%透明度 (0xCCFFFFFF)
//            int color = 0xCCFFFFFF;
//
//            // 渲染身体各部分
//            poseStack.pushPose();
//
//            // 应用玩家变换
//            float scale = 0.9375F;
//            poseStack.scale(scale, scale, scale);
//
//            // 渲染所有可见部分
//            model.renderToBuffer(poseStack, vertexConsumer, packedLight,
//                    net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, color);
//
//            poseStack.popPose();
//        }
//    }
//}