package com.phagens.corpseorigin.client.model;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * 尸化羽翼模型
 * 包含羽翼的动画系统
 */
public class WingModel extends ListModel<Player> {

    // 模型部件
    private final ModelPart Waist;
    private final ModelPart Body;
    private final ModelPart Corpsewings;
    private final ModelPart L;
    private final ModelPart bone;
    private final ModelPart bone4;
    private final ModelPart R;
    private final ModelPart bone2;
    private final ModelPart bone3;

    // 动画状态
    private float idleTime = 0;
    private boolean isFlying = false;

    public WingModel(ModelPart root) {
        // 安全检查 - 确保所有部件都存在
        this.Waist = root.hasChild("Waist") ? root.getChild("Waist") : root;
        this.Body = getChildIfExists(this.Waist, "Body");
        this.Corpsewings = getChildIfExists(this.Body, "Corpsewings");
        this.L = getChildIfExists(this.Corpsewings, "L");
        this.bone = getChildIfExists(this.L, "bone");
        this.bone4 = getChildIfExists(this.bone, "bone4");
        this.R = getChildIfExists(this.Corpsewings, "R");
        this.bone2 = getChildIfExists(this.R, "bone2");
        this.bone3 = getChildIfExists(this.bone2, "bone3");
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
        if (Corpsewings != null) builder.add(Corpsewings);
        return builder.build();
    }

    /**
     * 设置模型动画
     */
    @Override
    public void setupAnim(Player player, float limbSwing, float limbSwingAmount, 
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // 如果关键部件不存在，跳过动画
        if (Corpsewings == null) return;

        // 重置姿势
        resetPose();

        // 更新动画时间
        this.idleTime = ageInTicks * 0.05F;

        // 应用动画
        applyIdleAnimation(ageInTicks);
        applyWalkAnimation(limbSwing, limbSwingAmount);

        // 如果在飞行，应用飞行动画
        if (isFlying) {
            applyFlyingAnimation(ageInTicks);
        }
    }

    /**
     * 重置所有部件姿势
     */
    private void resetPose() {
        safeReset(Corpsewings);
        safeReset(L);
        safeReset(bone);
        safeReset(bone4);
        safeReset(R);
        safeReset(bone2);
        safeReset(bone3);
    }

    private void safeReset(ModelPart part) {
        if (part != null) {
            part.resetPose();
        }
    }

    /**
     * 待机动画 - 羽翼轻微摆动
     */
    private void applyIdleAnimation(float ageInTicks) {
        if (Corpsewings == null) return;

        float time = ageInTicks * 0.05F;

        // 羽翼轻微呼吸效果
        if (L != null) {
            float breathe = Mth.sin(time * 0.3F) * 0.02F;
            L.y += breathe;
        }
        if (R != null) {
            float breathe = Mth.sin(time * 0.3F + 0.5F) * 0.02F;
            R.y += breathe;
        }
    }

    /**
     * 行走动画 - 羽翼随步伐摆动
     */
    private void applyWalkAnimation(float limbSwing, float limbSwingAmount) {
        if (limbSwingAmount <= 0.01F) return;
        if (Corpsewings == null) return;

        float walkScale = Mth.PI / 8 * limbSwingAmount;

        // 羽翼随步伐摆动
        if (L != null) {
            L.zRot += Mth.cos(limbSwing * 0.5F) * walkScale * 0.3F;
        }
        if (R != null) {
            R.zRot += Mth.sin(limbSwing * 0.5F) * walkScale * 0.3F;
        }
    }

    /**
     * 飞行动画 - 羽翼扇动
     */
    private void applyFlyingAnimation(float ageInTicks) {
        if (L == null || R == null) return;

        float time = ageInTicks * 0.2F;
        float flap = Mth.sin(time) * 0.5F + 0.5F;

        // 左翼扇动
        if (L != null) {
            L.zRot = flap * -1.5708F; // -90度
        }
        // 右翼扇动
        if (R != null) {
            R.zRot = flap * 1.5708F; // 90度
        }

        // 骨骼联动
        if (bone != null) {
            bone.xRot = flap * -0.3927F; // -22.5度
        }
        if (bone2 != null) {
            bone2.xRot = flap * -0.3927F; // -22.5度
        }
    }

    /**
     * 触发飞行动画控制
     */
    public void setFlying(boolean flying) {
        this.isFlying = flying;
    }

    public boolean isFlying() {
        return isFlying;
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
        if (Corpsewings != null) {
            Corpsewings.render(poseStack, consumer, light, overlay, color);
        }
    }
}