package com.phagens.corpseorigin.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 尸化外骨骼模型
 * 包含 shieye 眼部和触须的动画系统
 */
public class ExoskeletonModel extends ListModel<Player> {

    // 模型部件 - 只保留实际使用的部件
    private final ModelPart Waist;
    private final ModelPart Head;
    private final ModelPart shieye;
    private final ModelPart group2;
    private final ModelPart group7;
    private final ModelPart group3;
    private final ModelPart group4;
    private final ModelPart group5;
    private final ModelPart group6;
    // 可选：如果需要渲染其他身体部分，但需要确保它们在模型定义中存在
    // private final ModelPart Body;
    // private final ModelPart RightArm;
    // private final ModelPart LeftArm;
    // private final ModelPart RightLeg;
    // private final ModelPart LeftLeg;

    // 动画状态
    private float idleTime = 0;
    private float shieyeProgress = 0;
    private boolean isShieyeActive = false;

    public ExoskeletonModel(ModelPart root) {
        // 安全检查 - 确保所有部件都存在
        this.Waist = root.hasChild("Waist") ? root.getChild("Waist") : root;

        // 处理 Head - 可能在 Waist 下，也可能直接在 root 下
        if (this.Waist.hasChild("Head")) {
            this.Head = this.Waist.getChild("Head");
        } else {
            this.Head = root.hasChild("Head") ? root.getChild("Head") : null;
        }

        // 处理 shieye - 可能在 Head 下，也可能直接在 root 下
        ModelPart shieyeParent = this.Head != null ? this.Head : root;
        if (shieyeParent.hasChild("shieye")) {
            this.shieye = shieyeParent.getChild("shieye");
        } else {
            this.shieye = root.hasChild("shieye") ? root.getChild("shieye") : root;
        }

        // 获取所有子部件，使用可选链式调用
        this.group2 = this.getChildIfExists(this.shieye, "group2");
        this.group7 = this.getChildIfExists(this.group2, "group7");
        this.group3 = this.getChildIfExists(this.group7, "group3");
        this.group4 = this.getChildIfExists(this.group3, "group4");
        this.group5 = this.getChildIfExists(this.group4, "group5");
        this.group6 = this.getChildIfExists(this.group4, "group6");
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
        if (shieye != null) builder.add(shieye);
        return builder.build();
    }

    /**
     * 设置模型动画
     */
    @Override
    public void setupAnim(Player player, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 如果关键部件不存在，跳过动画
        if (shieye == null) return;

        // 重置姿势
        resetPose();

        // 头部旋转（如果存在）
        if (Head != null) {
            Head.xRot = headPitch * ((float) Math.PI / 180F);
            Head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        }

        // 更新动画时间
        this.idleTime = ageInTicks * 0.05F;

        // 应用动画
        applyIdleAnimation(ageInTicks);
        applyWalkAnimation(limbSwing, limbSwingAmount);

        // 如果有攻击动作，应用攻击动画
        if (player.swingTime > 0) {
            applyAttackAnimation(player, ageInTicks);
        }

        // 应用 shieye 特殊动画
        if (isShieyeActive) {
            applyShieyeAnimation(ageInTicks);
        }
    }

    /**
     * 重置所有部件姿势
     */
    private void resetPose() {
        safeReset(Head);
        safeReset(shieye);
        safeReset(group2);
        safeReset(group7);
        safeReset(group3);
        safeReset(group4);
        safeReset(group5);
        safeReset(group6);
    }

    private void safeReset(ModelPart part) {
        if (part != null) {
            part.resetPose();
        }
    }

    /**
     * 待机动画 - 触须轻微摆动
     */
    private void applyIdleAnimation(float ageInTicks) {
        if (shieye == null) return;

        float time = ageInTicks * 0.05F;

        // 眼部轻微呼吸效果
        if (shieye != null) {
            float breathe = Mth.sin(time * 0.5F) * 0.02F;
            shieye.y += breathe;
        }

        // 触须第一段 - 轻微摆动
        if (group2 != null && group2 != shieye) {
            float wave1 = Mth.sin(time * 0.3F) * 0.05F;
            group2.xRot += wave1;
            group2.zRot += Mth.cos(time * 0.2F) * 0.03F;
        }

        // 触须第二段 - 延迟摆动
        if (group7 != null && group7 != group2) {
            float wave2 = Mth.sin(time * 0.25F + 0.5F) * 0.08F;
            group7.xRot += wave2;
            group7.yRot += Mth.sin(time * 0.15F) * 0.05F;
        }

        // 触须第三段 - 更大幅度的摆动
        if (group3 != null && group3 != group7) {
            float wave3 = Mth.sin(time * 0.2F + 1.0F) * 0.1F;
            group3.xRot += wave3;
            group3.zRot += Mth.cos(time * 0.18F) * 0.08F;
        }

        // 末端分叉 - 最灵活的摆动
        if (group4 != null && group4 != group3) {
            float wave4 = Mth.sin(time * 0.35F + 1.5F) * 0.12F;
            group4.yRot += wave4;
        }

        // 小分叉独立微动
        if (group5 != null && group5 != group4) {
            group5.zRot += Mth.sin(time * 0.4F) * 0.05F;
        }
        if (group6 != null && group6 != group4) {
            group6.zRot += Mth.cos(time * 0.45F) * 0.05F;
        }
    }

    /**
     * 行走动画 - 触须随步伐摆动
     */
    private void applyWalkAnimation(float limbSwing, float limbSwingAmount) {
        if (limbSwingAmount <= 0.01F) return;
        if (shieye == null) return;

        float walkScale = Mth.PI / 8 * limbSwingAmount;

        // 触须随步伐摆动
        if (group2 != null && group2 != shieye) {
            group2.xRot += Mth.cos(limbSwing * 0.5F) * walkScale * 0.5F;
        }
        if (group7 != null && group7 != group2) {
            group7.xRot += Mth.sin(limbSwing * 0.5F + 0.5F) * walkScale * 0.7F;
        }
        if (group3 != null && group3 != group7) {
            group3.xRot += Mth.cos(limbSwing * 0.5F + 1.0F) * walkScale;
        }

        // 垂直方向起伏
        float bob = Mth.sin(limbSwing * 0.5F) * 0.1F * limbSwingAmount;
        shieye.y += bob;
    }

    /**
     * 攻击动画
     */
    private void applyAttackAnimation(Player player, float ageInTicks) {
        float attackProgress = 1.0F - (player.swingTime / (float) player.hurtDuration);

        // 攻击时触须向前刺出
        float attackAngle = Mth.sin(attackProgress * Mth.PI) * 0.5F;

        if (group2 != null && group2 != shieye) {
            group2.xRot -= attackAngle * 0.8F;
        }
        if (group7 != null && group7 != group2) {
            group7.xRot -= attackAngle * 1.2F;
        }
        if (group3 != null && group3 != group7) {
            group3.xRot -= attackAngle * 1.5F;
        }

        // 末端分叉张开
        float spread = Mth.sin(attackProgress * Mth.PI) * 0.3F;
        if (group5 != null && group5 != group4) {
            group5.yRot += spread;
        }
        if (group6 != null && group6 != group4) {
            group6.yRot -= spread;
        }
    }

    /**
     * Shieye 特殊动画 - 触须伸展攻击
     */
    private void applyShieyeAnimation(float ageInTicks) {
        if (shieye == null) return;

        // 动画进度 0-3秒，循环
        float animTime = (ageInTicks * 0.05F) % 3.0F;

        // group2 旋转: 0 -> 17.5度
        if (group2 != null && group2 != shieye && animTime < 3.0F) {
            float t = Math.min(animTime / 3.0F, 1.0F);
            group2.xRot += t * 0.305F;
        }

        // group7 复杂动画
        if (group7 != null && group7 != group2) {
            if (animTime < 0.5F) {
                float t = animTime / 0.5F;
                group7.xRot += Mth.lerp(t, 0, -0.436F);
                group7.x += Mth.lerp(t, 0, 0.7F);
            } else if (animTime < 0.625F) {
                float t = (animTime - 0.5F) / 0.125F;
                group7.xRot = -0.436F + Mth.lerp(t, 0, -0.14F);
            } else if (animTime < 3.0F) {
                group7.xRot = -0.576F + 0.035F;
            }
        }

        // group3 旋转动画
        if (group3 != null && group3 != group7) {
            if (animTime < 0.5F) {
                float t = animTime / 0.5F;
                group3.xRot += Mth.lerp(t, 0, -0.89F);
                group3.zRot += Mth.lerp(t, 0, -1.568F);
            } else if (animTime < 3.0F) {
                float t = Math.min((animTime - 0.5F) / 2.5F, 1.0F);
                group3.xRot = -0.89F + Mth.lerp(t, 0, 0.61F);
                group3.yRot = Mth.lerp(t, 0, -0.397F);
                group3.zRot = -1.568F + Mth.lerp(t, 0, 0.445F);
            }
        }

        // group4 末端摆动
        if (group4 != null && group4 != group3) {
            if (animTime > 1.25F && animTime < 1.5F) {
                float t = (animTime - 1.25F) / 0.25F;
                group4.xRot += Mth.sin(t * Mth.PI) * -0.741F;
            } else if (animTime >= 3.0F) {
                group4.yRot += 0.523F;
            }
        }

        // group5, group6 缩放动画（钳子开合）
        if (animTime > 0.625F && animTime < 0.875F) {
            float t = (animTime - 0.625F) / 0.25F;
            float scale = 1.0F - Mth.sin(t * Mth.PI) * 0.35F;
            if (group5 != null && group5 != group4) {
                group5.zScale = scale;
            }
            if (group6 != null && group6 != group4) {
                group6.zScale = scale;
            }
        }
    }

    /**
     * 触发动画控制
     */
    public void setShieyeActive(boolean active) {
        this.isShieyeActive = active;
    }

    public boolean isShieyeActive() {
        return isShieyeActive;
    }

    /**
     * 复制头部模型姿势
     */
    public void copyFromHead(ModelPart head) {
        if (this.shieye != null) {
            this.shieye.copyFrom(head);
            // 调整位置到头部右侧
            this.shieye.x += 1.1F;
            this.shieye.y -= 0.3F;
            this.shieye.z += 2.0F;
        }
    }

    /**
     * 渲染模型
     */
    public void render(PoseStack poseStack, VertexConsumer consumer,
                       int light, int overlay, int color) {
        if (shieye != null) {
            shieye.render(poseStack, consumer, light, overlay, color);
        }
    }
}