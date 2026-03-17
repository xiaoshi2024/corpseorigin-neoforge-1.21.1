package com.phagens.corpseorigin.client.Renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * 尸兄外骨骼叠加层 - 复用 LowerLevelZbRenderer 的双层渲染逻辑
 * 第一层：玩家皮肤（100%不透明）
 * 第二层：尸化外骨骼（80%透明）
 */
public class CorpseSkeletonLayer<T extends LivingEntity & GeoAnimatable> extends GeoRenderLayer<T> {

    // 尸化外骨骼纹理
    public static final ResourceLocation SKELETON_NORMAL = ResourceLocation.fromNamespaceAndPath(
            "corpseorigin", "textures/entity/lower_level_zb_render.png");
    public static final ResourceLocation SKELETON_CRACKED = ResourceLocation.fromNamespaceAndPath(
            "corpseorigin", "textures/entity/lower_level_zb_rendering.png");

    private final ResourceLocation skinTexture;
    private final int variant;

    public CorpseSkeletonLayer(GeoRenderer<T> renderer, ResourceLocation skinTexture, int variant) {
        super(renderer);
        this.skinTexture = skinTexture;
        this.variant = variant;
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel,
                       RenderType renderType, MultiBufferSource bufferSource,
                       VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // 1. 渲染玩家皮肤底层（100% 不透明，白色混合）
        RenderType skinRenderType = RenderType.entityTranslucent(skinTexture);
        VertexConsumer skinConsumer = bufferSource.getBuffer(skinRenderType);

        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                skinRenderType, skinConsumer, partialTick, packedLight, packedOverlay,
                0xFFFFFFFF); // 白色，100% 不透明

        // 2. 叠加尸化外骨骼（80% 透明度）
        ResourceLocation overlay = variant == 1 ? SKELETON_CRACKED : SKELETON_NORMAL;
        RenderType skeletonRenderType = RenderType.entityTranslucent(overlay);
        VertexConsumer skeletonConsumer = bufferSource.getBuffer(skeletonRenderType);

        // 0xCCFFFFFF = ARGB格式，CC=204=80%透明度
        this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable,
                skeletonRenderType, skeletonConsumer, partialTick, packedLight, packedOverlay,
                0xCCFFFFFF);
    }
}