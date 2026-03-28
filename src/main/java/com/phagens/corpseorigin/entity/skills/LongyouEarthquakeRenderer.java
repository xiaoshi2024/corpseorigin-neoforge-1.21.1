package com.phagens.corpseorigin.entity.skills;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class LongyouEarthquakeRenderer extends EntityRenderer<LongyouEarthquakeEntity> {

    public LongyouEarthquakeRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(LongyouEarthquakeEntity entity, float yaw, float pticks, PoseStack matrices, MultiBufferSource src, int light) {
        super.render(entity, yaw, pticks, matrices, src, light);

        var blockInstances = entity.getBlockInstances();

        for (var entry : blockInstances.entrySet()) {
            var instance = entry.getValue();
            BlockPos pos = instance.getPos();
            BlockState blockState = instance.getBlockState();

            // 跳过空气
            if (blockState.isAir()) continue;

            renderFlippingBlock(entity, instance, pos, blockState, pticks, matrices, src);
        }
    }

    // 使用原版渲染器渲染方块，而不是手动画纹理
    private void renderFlippingBlock(LongyouEarthquakeEntity entity,
                                     LongyouEarthquakeEntity.EarthquakeBlockInstance instance,
                                     BlockPos pos,
                                     BlockState blockState,
                                     float pticks,
                                     PoseStack matrices,
                                     MultiBufferSource src) {

        // 获取方块在世界中的实际位置偏移
        Vec3 entityPos = entity.position();
        double offsetX = pos.getX() - entityPos.x + 0.5;
        double offsetZ = pos.getZ() - entityPos.z + 0.5;

        // 获取动画进度
        float progress = instance.getAnimationProgress(pticks);
        float heightOffset = instance.getHeightOffset(progress);
        float rotationAngle = instance.getRotationAngle(progress);

        matrices.pushPose();

        // 平移到方块位置
        matrices.translate(offsetX, pos.getY() - entityPos.y + heightOffset, offsetZ);

        // 获取旋转轴（指向中心的方向）
        Vec3 direction = instance.getDirection();
        float horizontalAngle = (float) Math.atan2(direction.x, direction.z);

        // 平移到方块中心进行旋转
        matrices.translate(0, 0.5, 0);

        // 绕Y轴旋转，使方块朝向中心
        matrices.mulPose(Axis.YP.rotation(horizontalAngle));

        // 绕X轴旋转，实现翻起效果
        matrices.mulPose(Axis.XP.rotationDegrees(rotationAngle));

        // 添加随机晃动效果
        float wobble = (float) Math.sin(progress * Math.PI * 3) * 8.0f * (1 - progress);
        matrices.mulPose(Axis.ZP.rotationDegrees(wobble));

        // 平移回方块角落
        matrices.translate(0, -0.5, 0);

        // 缩放效果
        float scale = 1.0f + (float) Math.sin(progress * Math.PI) * 0.15f;
        matrices.scale(scale, scale, scale);

        // 计算光照
        BlockPos lightPos = pos.above(2);
        int light = LightTexture.pack(
                entity.level().getMaxLocalRawBrightness(lightPos),
                entity.level().getLightEmission(lightPos)
        );

        // 使用原版渲染器渲染方块
        var renderer = Minecraft.getInstance().getBlockRenderer();
        renderer.renderSingleBlock(
                blockState,
                matrices,
                src,
                light,
                OverlayTexture.NO_OVERLAY,
                net.neoforged.neoforge.client.model.data.ModelData.EMPTY,
                null
        );

        matrices.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(LongyouEarthquakeEntity texture) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    @Override
    public boolean shouldRender(LongyouEarthquakeEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }
}