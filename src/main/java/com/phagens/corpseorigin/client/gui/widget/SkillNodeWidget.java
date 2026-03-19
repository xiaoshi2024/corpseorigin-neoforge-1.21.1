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
 * 技能树节点控件 - 使用16x16独立技能图标
 */
public class SkillNodeWidget extends AbstractWidget {

    private static final int NODE_SIZE = 32;
    private static final int ICON_SIZE = 16;
    private final ISkill skill;
    private NodeState state;
    private final int tier;
    private Font font;

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
        // 上边
        graphics.fill(getX(), getY(), getX() + width, getY() + 1, state.borderColor);
        // 下边
        graphics.fill(getX(), getY() + height - 1, getX() + width, getY() + height, state.borderColor);
        // 左边
        graphics.fill(getX(), getY(), getX() + 1, getY() + height, state.borderColor);
        // 右边
        graphics.fill(getX() + width - 1, getY(), getX() + width, getY() + height, state.borderColor);
    }

    private void renderSkillIcon(GuiGraphics graphics) {
        ResourceLocation iconPath = skill.getIcon();

        // 计算居中位置：节点大小32，图标大小16，所以偏移量为 (32-16)/2 = 8
        int iconX = getX() + 8;
        int iconY = getY() + 8;

        if (iconPath != null) {
            // 绘制16x16的技能图标
            graphics.blit(iconPath, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        } else {
            // 回退方案：使用技能类型颜色填充
            int color = getFallbackColor(skill.getSkillType());
            graphics.fill(iconX, iconY, iconX + ICON_SIZE, iconY + ICON_SIZE, color);

            // 绘制技能名称首字母
            String firstLetter = skill.getName().getString().substring(0, 1);
            graphics.drawString(font, firstLetter,
                    iconX + (ICON_SIZE - font.width(firstLetter)) / 2,
                    iconY + (ICON_SIZE - font.lineHeight) / 2,
                    0xFFFFFF, true);
        }
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
        graphics.drawString(font, tierText,
                getX() + (width - textWidth) / 2,
                getY() + height + 2,
                0xFFFFFF, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        this.defaultButtonNarrationText(narration);
    }
}