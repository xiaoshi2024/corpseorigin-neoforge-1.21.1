package com.phagens.corpseorigin.client.gui.widget;

import com.phagens.corpseorigin.skill.ISkill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 技能树节点控件 - 技能树界面中的单个技能节点UI组件
 *
 * 【功能说明】
 * 1. 显示技能图标（64x64像素）
 * 2. 根据节点状态显示不同颜色背景
 * 3. 显示技能层级（tier）
 * 4. 支持缩放以适应不同的缩放级别
 * 5. 图标加载失败时显示回退方案
 *
 * 【节点状态】
 * - LOCKED: 深灰色背景，浅灰色边框 - 未解锁
 * - AVAILABLE: 深绿色背景，浅绿色边框 - 可解锁
 * - UNLOCKED: 深紫色背景，浅紫色边框 - 已解锁
 *
 * 【渲染流程】
 * 1. 绘制背景色块
 * 2. 绘制边框
 * 3. 绘制技能图标（缩放适应）
 * 4. 绘制层级数字
 *
 * 【图标回退方案】
 * 当技能图标加载失败时：
 * 1. 使用技能类型对应的颜色填充
 * 2. 在中心显示技能名称首字母
 *
 * 【技能类型颜色】
 * - 基础进化：灰色
 * - 力量型变异：红色
 * - 敏捷型变异：绿色
 * - 特殊型变异：蓝色
 * - 神级能力：紫色
 * - 超神级能力：金色
 *
 * 【关联系统】
 * - SkillTreeScreen: 使用此控件构建技能树界面
 * - ISkill: 技能数据接口
 *
 * @author Phagens
 * @version 1.0
 */
public class SkillNodeWidget extends AbstractWidget {

    private static final int NODE_SIZE = 64;
    private static final int ICON_SIZE = 64;
    private final ISkill skill;
    private NodeState state;
    private final int tier;
    private Font font;
    private float scale = 1.0f;

    public enum NodeState {
        LOCKED(0xFF444444, 0xFF666666),
        AVAILABLE(0xFF44AA44, 0xFF66CC66),
        UNLOCKED(0xFFAA44AA, 0xFFCC66CC);

        public final int bgColor;
        public final int borderColor;

        NodeState(int bgColor, int borderColor) {
            this.bgColor = bgColor;
            this.borderColor = borderColor;
        }
    }

    public SkillNodeWidget(int x, int y, Component message,
                           ISkill skill, NodeState state,
                           int tier) {
        super(x, y, NODE_SIZE, NODE_SIZE, message);
        this.skill = skill;
        this.state = state;
        this.tier = tier;
        this.font = Minecraft.getInstance().font;
    }

    /**
     * 更新节点状态
     */
    public void setState(NodeState state) {
        this.state = state;
    }

    /**
     * 设置缩放比例
     */
    public void setScale(float scale) {
        this.scale = scale;
        int newSize = (int) (NODE_SIZE * scale);
        this.setWidth(newSize);
        this.setHeight(newSize);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 绘制节点背景
        renderNodeBackground(graphics);

        // 绘制节点边框
        renderNodeBorder(graphics);

        // 绘制技能图标（16x16居中显示）
        renderSkillIcon(graphics);

        // 绘制层级
        renderNodeTier(graphics);
    }

    private void renderNodeBackground(GuiGraphics graphics) {
        graphics.fill(getX(), getY(), getX() + width, getY() + height, state.bgColor);
    }

    private void renderNodeBorder(GuiGraphics graphics) {
        int borderThickness = Math.max(1, (int) scale);
        // 上边
        graphics.fill(getX(), getY(), getX() + width, getY() + borderThickness, state.borderColor);
        // 下边
        graphics.fill(getX(), getY() + height - borderThickness, getX() + width, getY() + height, state.borderColor);
        // 左边
        graphics.fill(getX(), getY(), getX() + borderThickness, getY() + height, state.borderColor);
        // 右边
        graphics.fill(getX() + width - borderThickness, getY(), getX() + width, getY() + height, state.borderColor);
    }

    private void renderSkillIcon(GuiGraphics graphics) {
        ResourceLocation iconPath = skill.getIcon();

        int scaledIconSize = (int) (ICON_SIZE * scale);

        if (iconPath != null) {
            // 绘制缩放的技能图标
            try {
                // 使用正确的blit参数顺序：resource, x, y, u, v, width, height, textureWidth, textureHeight
                graphics.blit(iconPath, getX(), getY(), 0, 0, scaledIconSize, scaledIconSize, scaledIconSize, scaledIconSize);
            } catch (Exception e) {
                // 回退方案：使用技能类型颜色填充
                renderFallbackIcon(graphics, scaledIconSize);
            }
        } else {
            // 回退方案：使用技能类型颜色填充
            renderFallbackIcon(graphics, scaledIconSize);
        }
    }

    private void renderFallbackIcon(GuiGraphics graphics, int size) {
        int color = getFallbackColor(skill.getSkillType());
        graphics.fill(getX(), getY(), getX() + size, getY() + size, color);

        // 绘制技能名称首字母
        String firstLetter = skill.getName().getString().substring(0, 1);
        float textScale = Math.max(0.5f, scale);
        int textX = getX() + (size - (int) (font.width(firstLetter) * textScale)) / 2;
        int textY = getY() + (size - (int) (font.lineHeight * textScale)) / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0);
        graphics.pose().scale(textScale, textScale, 1.0f);
        graphics.drawString(font, firstLetter, 0, 0, 0xFFFFFF, true);
        graphics.pose().popPose();
    }

    private int getFallbackColor(ISkill.SkillType type) {
        return switch (type) {
            case BASIC_EVOLUTION -> 0xFF666666;  // 灰色
            case POWER_MUTATION -> 0xFFAA4444;   // 红色
            case AGILITY_MUTATION -> 0xFF44AA44; // 绿色
            case SPECIAL_MUTATION -> 0xFF4444AA; // 蓝色
            case DIVINE_ABILITY -> 0xFFAA44AA;   // 紫色
            case SUPREME_ABILITY -> 0xFFFFAA00;  // 金色
        };
    }

    private void renderNodeTier(GuiGraphics graphics) {
        Component tierText = Component.literal(String.valueOf(tier));
        int textWidth = font.width(tierText);
        float textScale = Math.max(0.5f, scale);
        int textX = getX() + (width - (int) (textWidth * textScale)) / 2;
        int textY = getY() + height + (int) (2 * scale);

        graphics.pose().pushPose();
        graphics.pose().translate(textX, textY, 0);
        graphics.pose().scale(textScale, textScale, 1.0f);
        graphics.drawString(font, tierText, 0, 0, 0xFFFFFF, true);
        graphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        this.defaultButtonNarrationText(narration);
    }
}