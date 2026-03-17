//package com.phagens.corpseorigin.client.Renderer.entity;
//
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.blaze3d.vertex.VertexConsumer;
//import com.mojang.math.Axis;
//import com.phagens.corpseorigin.CorpseOrigin;
//import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
//import com.phagens.corpseorigin.client.Models.entity.CorpsePlayerGeoModel;
//import com.phagens.corpseorigin.client.skin.ZbSkinState;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.resources.ResourceLocation;
//import software.bernie.geckolib.cache.object.BakedGeoModel;
//import software.bernie.geckolib.renderer.GeoEntityRenderer;
//import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
//
///**
// * 尸族玩家Geo渲染器 - 使用GeckoLib模型 + 玩家皮肤叠加
// * 完全模拟 LowerLevelZbEntity 的渲染行为
// */
//public class CorpsePlayerGeoRenderer extends GeoEntityRenderer<CorpsePlayerGeoModel.CorpsePlayerProxy> {
//
//    // 尸化骨骼覆盖纹理（对应 LowerLevelZbEntity 的变种）
//    private static final ResourceLocation SKELETON_OVERLAY =
//            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_render.png");
//    private static final ResourceLocation CRACKED_SKELETON_OVERLAY =
//            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_rendering.png");
//
//    // 默认纹理（当没有皮肤时使用）
//    private static final ResourceLocation DEFAULT_TEXTURE =
//            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_render.png");
//
//    public CorpsePlayerGeoRenderer(EntityRendererProvider.Context context) {
//        super(context, new CorpsePlayerGeoModel());
//        this.shadowRadius = 0.5f;
//
//        // 添加尸化骨骼层
//        this.addRenderLayer(new CorpseSkeletonLayer(this));
//    }
//
//    /**
//     * 渲染实际的玩家
//     */
//    public void renderPlayer(AbstractClientPlayer player, float yaw, float partialTick,
//                             PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
//        // 创建代理对象
//        CorpsePlayerGeoModel.CorpsePlayerProxy proxy = new CorpsePlayerGeoModel.CorpsePlayerProxy(player);
//
//        // 更新位置到最新（确保渲染位置正确）
//        proxy.updatePositionAndRotation();
//
//        // 调用Geo渲染
//        this.render(proxy, yaw, partialTick, poseStack, buffer, packedLight);
//    }
//
//    @Override
//    public ResourceLocation getTextureLocation(CorpsePlayerGeoModel.CorpsePlayerProxy proxy) {
//        // 获取玩家皮肤
//        AbstractClientPlayer player = proxy.getPlayer();
//        if (player != null) {
//            return player.getSkin().texture();
//        }
//        return DEFAULT_TEXTURE;
//    }
//
//    @Override
//    public void render(CorpsePlayerGeoModel.CorpsePlayerProxy entity, float entityYaw, float partialTick,
//                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
//
//        // 在渲染前再次确保位置同步
//        entity.updatePositionAndRotation();
//
//        poseStack.pushPose();
//
//        // ========== 修复朝向问题 ==========
//        // 获取玩家的实际旋转
//        AbstractClientPlayer player = entity.getPlayer();
//        float bodyYaw = player.yBodyRot; // 身体旋转
//        float headYaw = player.getYRot(); // 头部旋转
//
//        // 1. 调整模型位置到正确高度（与 LowerLevelZbEntity 一致）
//        poseStack.translate(0.0D, 1.5D, 0.0D);
//
//        // 2. 应用旋转（与 LowerLevelZbEntity 保持一致）
//        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw)); // 修正朝向
//
//        // 3. 应用缩放（如果需要）
//        // poseStack.scale(1.0F, 1.0F, 1.0F);
//
//        // 调用父类渲染
//        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
//
//        poseStack.popPose();
//    }
//
//    /**
//     * 重写获取渲染类型的方法
//     */
//    @Override
//    public RenderType getRenderType(CorpsePlayerGeoModel.CorpsePlayerProxy animatable,
//                                    ResourceLocation texture,
//                                    MultiBufferSource bufferSource,
//                                    float partialTick) {
//        // 使用实体透明渲染类型，支持透明度
//        return RenderType.entityTranslucent(texture);
//    }
//
//    /**
//     * 尸化骨骼层 - 叠加外骨骼纹理
//     * 完全模拟 LowerLevelZbEntity 的变种纹理叠加
//     */
//    private class CorpseSkeletonLayer extends GeoRenderLayer<CorpsePlayerGeoModel.CorpsePlayerProxy> {
//        public CorpseSkeletonLayer(CorpsePlayerGeoRenderer renderer) {
//            super(renderer);
//        }
//
//        @Override
//        public void render(PoseStack poseStack, CorpsePlayerGeoModel.CorpsePlayerProxy proxy, BakedGeoModel bakedModel,
//                           RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
//                           float partialTick, int packedLight, int packedOverlay) {
//
//            // 获取变种类型（对应 LowerLevelZbEntity.Variant）
//            int variant = proxy.getVariant();
//
//            // 根据变种选择不同的纹理
//            ResourceLocation overlayTexture;
//            int color;
//
//            if (variant == 1) { // CRACKED
//                overlayTexture = CRACKED_SKELETON_OVERLAY;
//                color = 0xCCFFFFFF; // 80%透明度
//            } else { // NORMAL
//                overlayTexture = SKELETON_OVERLAY;
//                color = 0xCCFFFFFF; // 80%透明度
//            }
//
//            // 使用半透明渲染
//            RenderType skeletonRenderType = RenderType.entityTranslucent(overlayTexture);
//            VertexConsumer skeletonConsumer = bufferSource.getBuffer(skeletonRenderType);
//
//            // 重新渲染模型，叠加外骨骼纹理
//            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, proxy,
//                    skeletonRenderType, skeletonConsumer, partialTick, packedLight, packedOverlay,
//                    color);
//        }
//    }
//}