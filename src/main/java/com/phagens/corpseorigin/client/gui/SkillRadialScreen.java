package com.phagens.corpseorigin.client.gui;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.gui.radialmenu.*;
import com.phagens.corpseorigin.network.ActivateSkillPacket;
import com.phagens.corpseorigin.skill.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能轮盘界面 - 专业版
 * 使用分层渲染和专业渲染技术，同时保持独立图标
 */
public class SkillRadialScreen extends GuiRadialMenu<ISkill> {

    private final Player player;
    private final ISkillHandler skillHandler;

    public SkillRadialScreen(Player player) {
        super(createRadialMenu(player));
        this.player = player;
        this.skillHandler = SkillAttachment.getSkillHandler(player);
    }

    private static RadialMenu<ISkill> createRadialMenu(Player player) {
        ISkillHandler handler = SkillAttachment.getSkillHandler(player);
        List<IRadialMenuSlot<ISkill>> slots = new ArrayList<>();

        for (ISkill skill : handler.getLearnedSkills()) {
            if (skill.isActivatable()) {
                slots.add(new RadialMenuSlot<>(skill.getName(), skill));
            }
        }

        if (slots.isEmpty()) {
            slots.add(new RadialMenuSlot<>(
                    Component.translatable("skill.corpseorigin.no_activatable"), null));
        }

        return new RadialMenu<>(
                (index) -> onSkillSelected(player, index),
                slots,
                null,
                -1
        );
    }

    private static void onSkillSelected(Player player, int index) {
        ISkillHandler handler = SkillAttachment.getSkillHandler(player);
        List<ISkill> activatableSkills = new ArrayList<>();

        for (ISkill skill : handler.getLearnedSkills()) {
            if (skill.isActivatable()) {
                activatableSkills.add(skill);
            }
        }

        if (index >= 0 && index < activatableSkills.size()) {
            ISkill selectedSkill = activatableSkills.get(index);

            if (handler.isOnCooldown(selectedSkill)) {
                int remaining = handler.getCooldownRemaining(selectedSkill);
                player.sendSystemMessage(Component.translatable(
                        "skill.corpseorigin.cooldown", remaining / 20));
                return;
            }

            CorpseOrigin.LOGGER.debug("请求激活技能: {}", selectedSkill.getId());
            // 发送网络包
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new com.phagens.corpseorigin.network.ActivateSkillPacket(selectedSkill.getId()));
        }
    }

    @Override
    protected void drawIcon(GuiGraphics graphics, ISkill skill, int x, int y) {
        if (skill == null) {
            graphics.renderItem(new ItemStack(Items.BARRIER), x, y);
            return;
        }

        boolean onCooldown = skillHandler.isOnCooldown(skill);

        // 绘制技能图标
        ResourceLocation iconPath = skill.getIcon();
        if (iconPath != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 10); // 确保图标在顶层

            // 绘制16x16的技能图标
            graphics.blit(iconPath, x, y, 0, 0, 16, 16, 16, 16);

            graphics.pose().popPose();
        } else {
            graphics.renderItem(getSkillItemIcon(skill), x, y);
        }

        // 绘制冷却遮罩
        if (onCooldown) {
            int remaining = skillHandler.getCooldownRemaining(skill);

            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 20); // 冷却遮罩在最顶层

            graphics.fill(x, y, x + 16, y + 16, 0xAA000000);

            String cooldownText = String.valueOf(remaining / 20 + 1);
            int textWidth = minecraft.font.width(cooldownText);
            graphics.drawString(minecraft.font, cooldownText,
                    x + 8 - textWidth / 2, y + 4, 0xFFAAAA, true);

            graphics.pose().popPose();
        }
    }

    private ItemStack getSkillItemIcon(ISkill skill) {
        return switch (skill.getSkillType()) {
            case BASIC_EVOLUTION -> new ItemStack(Items.IRON_CHESTPLATE);
            case POWER_MUTATION -> new ItemStack(Items.DIAMOND_SWORD);
            case AGILITY_MUTATION -> new ItemStack(Items.FEATHER);
            case SPECIAL_MUTATION -> new ItemStack(Items.POTION);
            case DIVINE_ABILITY -> new ItemStack(Items.NETHER_STAR);
            case SUPREME_ABILITY -> new ItemStack(Items.DRAGON_EGG);
        };
    }

    @Override
    protected void drawSecondaryIcon(GuiGraphics graphics, ISkill icon, int x, int y) {
        // 技能没有次要图标
    }

    @Override
    protected int getSliceColor(IRadialMenuSlot<ISkill> slot, boolean highlighted, int index) {
        ISkill skill = slot.primarySlotIcon();

        if (skill == null) {
            return COLOR_DISABLED;
        }

        if (skillHandler.isOnCooldown(skill)) {
            return COLOR_COOLDOWN;
        }

        if (highlighted) {
            return COLOR_HIGHLIGHT;
        }

        return getColorBySkillType(skill.getSkillType());
    }

    private int getColorBySkillType(ISkill.SkillType type) {
        return switch (type) {
            case BASIC_EVOLUTION -> 0xCC666666;
            case POWER_MUTATION -> 0xCCAA4444;
            case AGILITY_MUTATION -> 0xCC44AA44;
            case SPECIAL_MUTATION -> 0xCC4444AA;
            case DIVINE_ABILITY -> 0xCCAA44AA;
            case SUPREME_ABILITY -> 0xCCFFAA00;
        };
    }

    @Override
    protected void renderSelectedInfo(GuiGraphics graphics) {
        if (selectedItem >= 0 && selectedItem < radialMenuSlots.size()) {
            IRadialMenuSlot<ISkill> slot = radialMenuSlots.get(selectedItem);
            ISkill skill = slot.primarySlotIcon();

            if (skill == null) return;

            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, Z_TEXT);

            // 技能名称
            Component name = skill.getName();
            int textWidth = minecraft.font.width(name);
            int x = (this.width - textWidth) / 2;
            int y = this.height - 65;

            graphics.fill(x - 5, y - 5, x + textWidth + 5, y + 50, 0xCC000000);
            graphics.renderOutline(x - 5, y - 5, textWidth + 10, 55, 0xFFFFFFFF);

            graphics.drawString(minecraft.font, name, x, y, 0xFFFFFF, true);

            // 技能描述
            if (skill.getDescription() != null) {
                Component desc = skill.getDescription();
                int descWidth = minecraft.font.width(desc);
                int descX = (this.width - descWidth) / 2;
                graphics.drawString(minecraft.font, desc, descX, y + 15, 0xAAAAAA, true);
            }

            // 冷却信息
            if (skillHandler.isOnCooldown(skill)) {
                int remaining = skillHandler.getCooldownRemaining(skill);
                Component cooldownText = Component.translatable(
                        "skill.corpseorigin.cooldown_remaining", remaining / 20);
                int cdWidth = minecraft.font.width(cooldownText);
                int cdX = (this.width - cdWidth) / 2;
                graphics.drawString(minecraft.font, cooldownText, cdX, y + 30, 0xFF4444, true);
            }

            graphics.pose().popPose();
        }
    }

    public static void show() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new SkillRadialScreen(minecraft.player));
        }
    }
}