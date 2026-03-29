package com.phagens.corpseorigin.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 多眼系统模型
 * 像盔甲层一样绑定到Head骨骼
 * 进化后显示全部9个眼睛
 */
public class MultiEyeModel extends ListModel<Player> {

    // 模型部件
    private final ModelPart MultipleEyes;

    // 9个眼睛组
    private final ModelPart group30;
    private final ModelPart group2;
    private final ModelPart group6;
    private final ModelPart group10;
    private final ModelPart group14;
    private final ModelPart group18;
    private final ModelPart group22;
    private final ModelPart group26;
    private final ModelPart group34;

    // 动画状态
    private float idleTime = 0;
    private boolean hasMultiEye = false;

    public MultiEyeModel(ModelPart root) {
        // 直接获取MultipleEyes
        this.MultipleEyes = root.getChild("MultipleEyes");

        // 获取所有眼睛组
        this.group30 = this.MultipleEyes.getChild("group30");
        this.group2 = this.MultipleEyes.getChild("group2");
        this.group6 = this.MultipleEyes.getChild("group6");
        this.group10 = this.MultipleEyes.getChild("group10");
        this.group14 = this.MultipleEyes.getChild("group14");
        this.group18 = this.MultipleEyes.getChild("group18");
        this.group22 = this.MultipleEyes.getChild("group22");
        this.group26 = this.MultipleEyes.getChild("group26");
        this.group34 = this.MultipleEyes.getChild("group34");
    }

    @Override
    public Iterable<ModelPart> parts() {
        // 进化后返回所有眼睛组
        if (!hasMultiEye) {
            return ImmutableList.of();
        }
        return ImmutableList.of(
                group30, group2, group6, group10, group14,
                group18, group22, group26, group34
        );
    }

    /**
     * 设置是否有多眼形态
     */
    public void setHasMultiEye(boolean hasMultiEye) {
        this.hasMultiEye = hasMultiEye;
    }

    /**
     * 获取是否有多眼形态
     */
    public boolean hasMultiEye() {
        return hasMultiEye;
    }

    /**
     * 设置模型动画
     * 所有9个眼睛同时动画
     */
    @Override
    public void setupAnim(Player player, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        if (!hasMultiEye) {
            return;
        }

        // 更新空闲时间
        idleTime += 0.05f;

        // 为所有9个眼睛组设置动画
        animateEyeGroup(group30, 0, ageInTicks);
        animateEyeGroup(group2, 1, ageInTicks);
        animateEyeGroup(group6, 2, ageInTicks);
        animateEyeGroup(group10, 3, ageInTicks);
        animateEyeGroup(group14, 4, ageInTicks);
        animateEyeGroup(group18, 5, ageInTicks);
        animateEyeGroup(group22, 6, ageInTicks);
        animateEyeGroup(group26, 7, ageInTicks);
        animateEyeGroup(group34, 8, ageInTicks);
    }

    /**
     * 动画化单个眼睛组
     * 动画幅度很小，保持静态感
     */
    private void animateEyeGroup(ModelPart eyeGroup, int index, float ageInTicks) {
        if (eyeGroup == null) return;

        // 基础摆动 - 每个眼睛有不同的相位
        float offset = index * 0.7f;

        // 眼睛微动动画 - 幅度很小，保持静态
        float rotX = Mth.sin(idleTime * 0.5f + offset) * 0.02f;
        float posZ = Mth.cos(idleTime * 0.3f + offset) * 0.02f;

        eyeGroup.xRot += rotX;
        eyeGroup.z += posZ;

        // 子部件动画
        animateEyeChildren(eyeGroup, index);
    }

    /**
     * 动画化眼睛的子部件
     * 动画幅度很小，保持静态感
     */
    private void animateEyeChildren(ModelPart parent, int index) {
        if (parent == null) return;

        // 获取子部件
        ModelPart child1 = parent.hasChild("group" + (31 + index * 4)) ?
                parent.getChild("group" + (31 + index * 4)) : null;
        ModelPart child2 = parent.hasChild("group" + (32 + index * 4)) ?
                parent.getChild("group" + (32 + index * 4)) : null;

        // 子部件动画 - 幅度很小
        if (child1 != null) {
            float rotY = Mth.sin(idleTime * 0.3f + index) * 0.03f;
            child1.yRot += rotY;
        }

        if (child2 != null) {
            float posZ = Mth.sin(idleTime * 0.4f + index * 0.5f) * 0.01f;
            child2.z += posZ;
        }
    }

    /**
     * 渲染模型
     * 像盔甲层一样，已经在正确的坐标系中
     */
    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight,
                       int packedOverlay, int color) {
        // 进化后渲染所有眼睛
        if (MultipleEyes != null && hasMultiEye) {
            MultipleEyes.render(poseStack, consumer, packedLight, packedOverlay, color);
        }
    }
}
