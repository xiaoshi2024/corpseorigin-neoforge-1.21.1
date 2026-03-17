package com.phagens.corpseorigin.client.gui;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.gui.radialmenu.*;
import com.phagens.corpseorigin.network.ActivateSkillPacket;
import com.phagens.corpseorigin.skill.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * 技能轮盘界面 - 用于选择和激活技能
 */
public class SkillRadialScreen extends GuiRadialMenu<ISkill> {
    
    private static final ResourceLocation SKILL_ICONS = 
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/gui/skill_icons.png");
    
    private final Player player;
    private final ISkillHandler skillHandler;
    
    public SkillRadialScreen(Player player) {
        super(createRadialMenu(player));
        this.player = player;
        this.skillHandler = SkillAttachment.getSkillHandler(player);
    }
    
    /**
     * 创建技能轮盘菜单
     */
    private static RadialMenu<ISkill> createRadialMenu(Player player) {
        ISkillHandler handler = SkillAttachment.getSkillHandler(player);
        List<IRadialMenuSlot<ISkill>> slots = new ArrayList<>();
        
        // 获取已学习的可激活技能
        for (ISkill skill : handler.getLearnedSkills()) {
            if (skill.isActivatable()) {
                slots.add(new RadialMenuSlot<>(skill.getName(), skill));
            }
        }
        
        // 如果没有可激活技能，显示提示
        if (slots.isEmpty()) {
            slots.add(new RadialMenuSlot<>(Component.translatable("skill.corpseorigin.no_activatable"), null));
        }
        
        return new RadialMenu<>(
                (index) -> onSkillSelected(player, index),
                slots,
                SkillRadialScreen::drawSkillIcon,
                -1
        );
    }
    
    /**
     * 技能选择回调
     */
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
            
            // 检查冷却
            if (handler.isOnCooldown(selectedSkill)) {
                int remaining = handler.getCooldownRemaining(selectedSkill);
                player.sendSystemMessage(Component.translatable(
                        "skill.corpseorigin.cooldown", remaining / 20));
                return;
            }
            
            // 发送网络包到服务器激活技能
            ActivateSkillPacket packet = new ActivateSkillPacket(selectedSkill.getId());
            // 通过网络发送包
            CorpseOrigin.LOGGER.debug("请求激活技能: {}", selectedSkill.getId());
        }
    }
    
    /**
     * 绘制技能图标
     */
    private static Void drawSkillIcon(ISkill skill) {
        // 这里可以添加自定义的图标绘制逻辑
        return null;
    }
    
    @Override
    protected void drawIcon(GuiGraphics graphics, ISkill skill, int x, int y) {
        if (skill == null) {
            // 绘制空图标
            graphics.renderItem(new ItemStack(Items.BARRIER), x - 8, y - 8);
            return;
        }
        
        // 检查技能是否在冷却中
        boolean onCooldown = skillHandler.isOnCooldown(skill);
        
        // 根据技能类型选择图标
        ItemStack iconStack = getSkillIcon(skill);
        
        // 绘制物品图标
        graphics.renderItem(iconStack, x - 8, y - 8);
        
        // 如果在冷却中，绘制冷却遮罩
        if (onCooldown) {
            int remaining = skillHandler.getCooldownRemaining(skill);
            float cooldownPercent = (float) remaining / skill.getCooldown();
            
            // 绘制半透明黑色遮罩
            graphics.fill(x - 8, y - 8, x + 8, y + 8, 0xAA000000);
            
            // 绘制冷却时间文字
            String cooldownText = String.valueOf(remaining / 20 + 1);
            int textWidth = minecraft.font.width(cooldownText);
            graphics.drawString(minecraft.font, cooldownText, 
                    x - textWidth / 2, y - 4, 0xFFAAAA, true);
        }
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
        
        // 检查冷却
        if (skillHandler.isOnCooldown(skill)) {
            return COLOR_COOLDOWN;
        }
        
        // 根据技能类型设置颜色
        if (highlighted) {
            return COLOR_HIGHLIGHT;
        }
        
        return getColorBySkillType(skill.getSkillType());
    }
    
    /**
     * 根据技能类型获取颜色
     */
    private int getColorBySkillType(ISkill.SkillType type) {
        return switch (type) {
            case BASIC_EVOLUTION -> 0xFF666666;  // 灰色
            case POWER_MUTATION -> 0xFFAA4444;   // 红色
            case AGILITY_MUTATION -> 0xFF44AA44; // 绿色
            case SPECIAL_MUTATION -> 0xFF4444AA; // 蓝色
            case DIVINE_ABILITY -> 0xFFAA44AA;   // 紫色
            case SUPREME_ABILITY -> 0xFFFFAA00;  // 金色
        };
    }
    
    /**
     * 获取技能图标
     */
    private ItemStack getSkillIcon(ISkill skill) {
        // 根据技能类型返回不同的图标
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
    protected void renderSelectedInfo(GuiGraphics graphics) {
        if (selectedItem >= 0 && selectedItem < radialMenuSlots.size()) {
            IRadialMenuSlot<ISkill> slot = radialMenuSlots.get(selectedItem);
            ISkill skill = slot.primarySlotIcon();
            
            if (skill == null) return;
            
            // 绘制技能名称
            Component name = skill.getName();
            int textWidth = minecraft.font.width(name);
            int x = (this.width - textWidth) / 2;
            int y = this.height - 60;
            
            graphics.fill(x - 5, y - 5, x + textWidth + 5, y + 15, 0xAA000000);
            graphics.drawString(minecraft.font, name, x, y, 0xFFFFFF, true);
            
            // 绘制技能描述
            if (skill.getDescription() != null) {
                Component desc = skill.getDescription();
                int descWidth = minecraft.font.width(desc);
                int descX = (this.width - descWidth) / 2;
                int descY = y + 15;
                
                graphics.drawString(minecraft.font, desc, descX, descY, 0xAAAAAA, true);
            }
            
            // 显示冷却信息
            if (skillHandler.isOnCooldown(skill)) {
                int remaining = skillHandler.getCooldownRemaining(skill);
                Component cooldownText = Component.translatable(
                        "skill.corpseorigin.cooldown_remaining", remaining / 20);
                int cdWidth = minecraft.font.width(cooldownText);
                int cdX = (this.width - cdWidth) / 2;
                int cdY = y + 30;
                
                graphics.drawString(minecraft.font, cooldownText, cdX, cdY, 0xFF4444, true);
            }
        }
    }
    
    /**
     * 显示技能轮盘
     */
    public static void show() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new SkillRadialScreen(minecraft.player));
        }
    }
}
