package com.phagens.corpseorigin.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderArmEvent;

//渲染玩家第一人称手臂尸化模型
@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class CorpseFirstPersonHandler {

    private static final ResourceLocation SKELETON_NORMAL =
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_render.png");
    private static final ResourceLocation SKELETON_CRACKED =
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/lower_level_zb_rendering.png");

    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = event.getPlayer();

        if (player == null || !PlayerCorpseData.isCorpse(player)) {
            return;
        }

        // 取消原版渲染
        event.setCanceled(true);

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        int packedLight = event.getPackedLight();
        HumanoidArm arm = event.getArm();

        // 获取玩家渲染器
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        EntityRenderer<?> renderer = dispatcher.getRenderer(player);

        if (!(renderer instanceof PlayerRenderer playerRenderer)) {
            return;
        }

        // 获取纹理
        ResourceLocation playerSkin = player.getSkin().texture();
        int variant = PlayerCorpseData.getVariant(player);
        ResourceLocation skeletonTexture = variant == 1 ? SKELETON_CRACKED : SKELETON_NORMAL;

        // 保存当前矩阵状态
        poseStack.pushPose();

        // 渲染皮肤层 - 使用原版方法
        renderHandWithTexture(playerRenderer, poseStack, bufferSource, packedLight, player, arm, playerSkin, 0xFFFFFFFF);

        // 渲染外骨骼层 - 80%透明度
        renderHandWithTexture(playerRenderer, poseStack, bufferSource, packedLight, player, arm, skeletonTexture, 0xCCFFFFFF);

        poseStack.popPose();
    }

    /**
     * 完全按照原版 renderHand 方法的逻辑渲染手臂
     */
    private static void renderHandWithTexture(PlayerRenderer playerRenderer, PoseStack poseStack,
                                              MultiBufferSource bufferSource, int packedLight,
                                              AbstractClientPlayer player, HumanoidArm arm,
                                              ResourceLocation texture, int color) {

        PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();

        // 1. 设置模型属性（完全按照原版）
        // 注意：这里不能调用 setModelProperties，因为它是私有的，但我们只设置需要的属性
        model.setAllVisible(false);
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;

        // 2. 设置手臂可见性
        ModelPart armPart;
        ModelPart sleevePart;

        if (arm == HumanoidArm.RIGHT) {
            model.rightArm.visible = true;
            model.rightSleeve.visible = true;
            armPart = model.rightArm;
            sleevePart = model.rightSleeve;
        } else {
            model.leftArm.visible = true;
            model.leftSleeve.visible = true;
            armPart = model.leftArm;
            sleevePart = model.leftSleeve;
        }

        // 3. 更新模型动画（重要！）
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // 4. 重置手臂旋转
        armPart.xRot = 0.0F;
        sleevePart.xRot = 0.0F;

        // 5. 渲染手臂（实体纹理）
        RenderType armRenderType = RenderType.entitySolid(texture);
        VertexConsumer armBuffer = bufferSource.getBuffer(armRenderType);
        armPart.render(poseStack, armBuffer, packedLight, OverlayTexture.NO_OVERLAY, color);

        // 6. 渲染袖子（透明纹理）
        RenderType sleeveRenderType = RenderType.entityTranslucent(texture);
        VertexConsumer sleeveBuffer = bufferSource.getBuffer(sleeveRenderType);
        sleevePart.render(poseStack, sleeveBuffer, packedLight, OverlayTexture.NO_OVERLAY, color);

        // 7. 恢复模型可见性（虽然不是必须，但为了安全）
        model.setAllVisible(true);
    }
}