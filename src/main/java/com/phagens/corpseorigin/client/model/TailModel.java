package com.phagens.corpseorigin.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 尸化鱼尾模型
 * 包含鱼尾的动画系统
 */
public class TailModel extends ListModel<Player> {

    // 模型部件
    private final ModelPart Waist;
    private final ModelPart Body;
    private final ModelPart Corpsetail;
    private final ModelPart dorsal;
    private final ModelPart bone;

    // 动画状态
    private float idleTime = 0;
    private boolean isSwimming = false;

    public TailModel(ModelPart root) {
        // 安全检查 - 确保所有部件都存在
        this.Waist = root.hasChild("Waist") ? root.getChild("Waist") : root;
        this.Body = getChildIfExists(this.Waist, "Body");
        this.Corpsetail = getChildIfExists(this.Body, "Corpsetail");
        this.dorsal = getChildIfExists(this.Corpsetail, "dorsal");
        this.bone = getChildIfExists(this.Corpsetail, "bone");
    }

    /**
     * 安全获取子部件，如果不存在则返回父部件
     */
    private ModelPart getChildIfExists(ModelPart parent, String childName) {
        if (parent != null && parent.hasChild(childName)) {
            return parent.getChild(childName);
        }
        return parent; // 返回父部件作为备选
    }

    @Override
    public Iterable<ModelPart> parts() {
        // 只返回实际存在的部件
        ImmutableList.Builder<ModelPart> builder = ImmutableList.builder();
        if (Corpsetail != null) builder.add(Corpsetail);
        return builder.build();
    }

    /**
     * 设置模型动画
     */
    @Override
    public void setupAnim(Player player, float limbSwing, float limbSwingAmount, 
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 如果关键部件不存在，跳过动画
        if (Corpsetail == null) return;

        // 重置姿势
        resetPose();

        // 更新动画时间
        this.idleTime = ageInTicks * 0.05F;

        // 应用动画
        applyIdleAnimation(ageInTicks);
        applyWalkAnimation(limbSwing, limbSwingAmount);

        // 如果在游泳，应用游泳动画
        if (isSwimming) {
            applySwimmingAnimation(ageInTicks);
        }
    }

    /**
     * 重置所有部件姿势
     */
    private void resetPose() {
        safeReset(Corpsetail);
        safeReset(dorsal);
        safeReset(bone);
    }

    private void safeReset(ModelPart part) {
        if (part != null) {
            part.resetPose();
        }
    }

    /**
     * 待机动画 - 鱼尾轻微摆动
     */
    private void applyIdleAnimation(float ageInTicks) {
        if (Corpsetail == null) return;

        float time = ageInTicks * 0.05F;

        // 鱼尾轻微摆动
        if (bone != null) {
            float wave = Mth.sin(time * 0.2F) * 0.1F;
            bone.xRot += wave;
        }
        if (dorsal != null) {
            float wave = Mth.sin(time * 0.2F + 0.3F) * 0.05F;
            dorsal.xRot += wave;
        }
    }

    /**
     * 行走动画 - 鱼尾随步伐摆动
     */
    private void applyWalkAnimation(float limbSwing, float limbSwingAmount) {
        if (limbSwingAmount <= 0.01F) return;
        if (Corpsetail == null) return;

        float walkScale = Mth.PI / 8 * limbSwingAmount;

        // 鱼尾随步伐摆动
        if (bone != null) {
            bone.xRot += Mth.cos(limbSwing * 0.5F) * walkScale * 0.2F;
        }
    }

    /**
     * 游泳动画 - 鱼尾摆动
     */
    private void applySwimmingAnimation(float ageInTicks) {
        if (bone == null) return;

        float time = ageInTicks * 0.2F;
        float wave = Mth.sin(time) * 0.5F;

        // 鱼尾摆动
        if (bone != null) {
            bone.xRot = wave * 0.6283F; // 36度
        }
        if (dorsal != null) {
            dorsal.xRot = 0.7156F + wave * 0.1745F; // 41度 + 10度摆动
        }
    }

    /**
     * 触发游泳动画控制
     */
    public void setSwimming(boolean swimming) {
        this.isSwimming = swimming;
    }

    public boolean isSwimming() {
        return isSwimming;
    }

    /**
     * 复制身体模型姿势
     */
    public void copyFromBody(ModelPart body) {
        if (this.Body != null && body != null) {
            this.Body.copyFrom(body);
        }
    }

    /**
     * 渲染模型
     */
    public void render(PoseStack poseStack, VertexConsumer consumer, 
                       int light, int overlay, int color) {
        if (Corpsetail != null) {
            Corpsetail.render(poseStack, consumer, light, overlay, color);
        }
    }
}