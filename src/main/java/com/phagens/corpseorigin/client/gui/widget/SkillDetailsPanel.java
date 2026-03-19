package com.phagens.corpseorigin.client.gui.widget;

import com.phagens.corpseorigin.skill.ISkill;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * 技能详情面板
 */
public class SkillDetailsPanel extends AbstractWidget {

    private ISkill skill;
    private final ResourceLocation texture;
    private boolean isLearned;
    private boolean canUnlock;
    private Font font;

    public SkillDetailsPanel(int x, int y, int width, int height, ResourceLocation texture) {
        super(x, y, width, height, Component.empty());
        this.texture = texture;
        this.font = Minecraft.getInstance().font;
    }

    public void setSkill(ISkill skill, boolean isLearned, boolean canUnlock) {
        this.skill = skill;
        this.isLearned = isLearned;
        this.canUnlock = canUnlock;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (skill == null) return;

        // 绘制背景
        graphics.blit(texture, getX(), getY(), 0, 0, width, height, 256, 256);

        int y = getY() + 5;

        // 绘制技能名称
        graphics.drawString(font, skill.getName(), getX() + 5, y, 0xFFFFFF, true);
        y += 15;

        // 绘制技能描述
        if (skill.getDescription() != null) {
            String desc = skill.getDescription().getString();
            List<FormattedCharSequence> lines = font.split(Component.literal(desc), width - 10);
            for (FormattedCharSequence line : lines) {
                graphics.drawString(font, line, getX() + 5, y, 0xAAAAAA, true);
                y += 10;
            }
            y += 5;
        }

        // 绘制消耗
        graphics.drawString(font,
                Component.translatable("gui.corpseorigin.cost", skill.getCost()),
                getX() + 5, y, 0xFFD700, true);
        y += 12;

        // 绘制类型
        String typeKey = "skilltype.corpseorigin." + skill.getSkillType().name().toLowerCase();
        graphics.drawString(font,
                Component.translatable("gui.corpseorigin.type",
                        Component.translatable(typeKey)),
                getX() + 5, y, 0xAAAAAA, true);
        y += 15;

        // 绘制状态
        Component status;
        int statusColor;
        if (isLearned) {
            status = Component.translatable("gui.corpseorigin.unlocked");
            statusColor = 0x00FF00;
        } else if (canUnlock) {
            status = Component.translatable("gui.corpseorigin.can_unlock");
            statusColor = 0xFFFF00;
        } else {
            status = Component.translatable("gui.corpseorigin.locked");
            statusColor = 0xFF0000;
        }
        graphics.drawString(font, status, getX() + 5, y, statusColor, true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narration) {
        if (skill != null) {
            this.defaultButtonNarrationText(narration);
        }
    }
}