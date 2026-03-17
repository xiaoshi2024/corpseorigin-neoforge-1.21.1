package com.phagens.corpseorigin.client.gui;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.SkillUnlockPacket;
import com.phagens.corpseorigin.skill.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.util.*;

/**
 * 技能树界面
 */
public class SkillTreeScreen extends Screen {
    
    private static final ResourceLocation BACKGROUND_TEXTURE = 
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/gui/skill_tree_bg.png");
    
    // 节点大小
    private static final int NODE_SIZE = 32;
    private static final int NODE_SPACING_X = 80;
    private static final int NODE_SPACING_Y = 70;
    
    // 滚动偏移
    private double scrollX = 0;
    private double scrollY = 0;
    private boolean isDragging = false;
    private double lastMouseX;
    private double lastMouseY;
    
    // 选中的技能
    private ISkill selectedSkill = null;
    
    // 技能树数据
    private ISkillTree skillTree;
    private ISkillHandler skillHandler;
    
    // 解锁按钮
    private Button unlockButton;
    
    public SkillTreeScreen() {
        super(Component.translatable("skilltree.corpseorigin.corpse_evolution"));
    }
    
    @Override
    protected void init() {
        super.init();
        
        // 获取技能树和处理器
        skillTree = SkillManager.getInstance().getCorpseEvolutionTree();
        if (minecraft != null && minecraft.player != null) {
            skillHandler = SkillManager.getInstance().getSkillHandler(minecraft.player);
        }
        
        // 创建解锁按钮
        unlockButton = Button.builder(
                Component.translatable("gui.corpseorigin.unlock"),
                this::onUnlockButtonClicked)
            .pos(this.width / 2 - 50, this.height - 40)
            .size(100, 20)
            .build();
        
        this.addRenderableWidget(unlockButton);
        unlockButton.active = false;
        
        // 初始化滚动位置到中心
        if (skillTree != null) {
            scrollX = this.width / 2.0;
            scrollY = 100;
        }
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 渲染深色背景（不透明）
        graphics.fill(0, 0, this.width, this.height, FastColor.ARGB32.color(255, 16, 16, 24));

        // 渲染技能树
        renderSkillTree(graphics, mouseX, mouseY);

        // 渲染UI元素（按钮等）
        super.render(graphics, mouseX, mouseY, partialTick);

        // 渲染技能详情
        renderSkillDetails(graphics);

        // 渲染进化点数
        renderEvolutionPoints(graphics);

        // 渲染标题
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFD700);

        // 渲染提示
        renderTooltip(graphics, mouseX, mouseY);
    }
    
    /**
     * 渲染技能树
     */
    private void renderSkillTree(GuiGraphics graphics, int mouseX, int mouseY) {
        if (skillTree == null) return;
        
        // 渲染连接线
        renderConnections(graphics);
        
        // 渲染节点
        for (ISkillNode node : skillTree.getAllNodes()) {
            renderNode(graphics, node, mouseX, mouseY);
        }
    }
    
    /**
     * 渲染节点之间的连接线
     */
    private void renderConnections(GuiGraphics graphics) {
        if (!(skillTree instanceof SkillTree tree)) return;
        
        for (ISkillNode node : skillTree.getAllNodes()) {
            List<ISkillNode> parents = tree.getParentNodes(node);
            
            for (ISkillNode parent : parents) {
                int x1 = (int) (scrollX + parent.getX() * NODE_SPACING_X);
                int y1 = (int) (scrollY + parent.getY() * NODE_SPACING_Y);
                int x2 = (int) (scrollX + node.getX() * NODE_SPACING_X);
                int y2 = (int) (scrollY + node.getY() * NODE_SPACING_Y);
                
                // 根据解锁状态决定线的颜色
                int color;
                if (isNodeUnlocked(node)) {
                    color = FastColor.ARGB32.color(255, 100, 200, 100); // 绿色 - 已解锁
                } else if (canUnlockNode(node)) {
                    color = FastColor.ARGB32.color(255, 200, 200, 100); // 黄色 - 可解锁
                } else {
                    color = FastColor.ARGB32.color(255, 100, 100, 100); // 灰色 - 未解锁
                }
                
                // 绘制连接线
                drawLine(graphics, x1, y1, x2, y2, color);
            }
        }
    }
    
    /**
     * 绘制线条
     */
    private void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color) {
        // 简单的线条绘制
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            graphics.fill(x1 - 1, y1 - 1, x1 + 1, y1 + 1, color);

            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
    }
    
    /**
     * 渲染单个节点
     */
    private void renderNode(GuiGraphics graphics, ISkillNode node, int mouseX, int mouseY) {
        int x = (int) (scrollX + node.getX() * NODE_SPACING_X) - NODE_SIZE / 2;
        int y = (int) (scrollY + node.getY() * NODE_SPACING_Y) - NODE_SIZE / 2;
        
        // 检查鼠标是否悬停
        boolean hovered = mouseX >= x && mouseX <= x + NODE_SIZE && 
                         mouseY >= y && mouseY <= y + NODE_SIZE;
        
        // 获取节点状态颜色
        int borderColor;
        int bgColor;
        
        if (isNodeUnlocked(node)) {
            // 已解锁 - 绿色
            borderColor = FastColor.ARGB32.color(255, 50, 200, 50);
            bgColor = FastColor.ARGB32.color(255, 30, 100, 30);
        } else if (canUnlockNode(node)) {
            // 可解锁 - 黄色
            borderColor = FastColor.ARGB32.color(255, 200, 200, 50);
            bgColor = FastColor.ARGB32.color(255, 100, 100, 30);
        } else {
            // 未解锁 - 灰色
            borderColor = FastColor.ARGB32.color(255, 100, 100, 100);
            bgColor = FastColor.ARGB32.color(255, 50, 50, 50);
        }
        
        // 如果鼠标悬停，高亮边框
        if (hovered) {
            borderColor = FastColor.ARGB32.color(255, 255, 255, 255);
            
            // 选中技能
            if (!node.getSkills().isEmpty()) {
                selectedSkill = node.getSkills().get(0);
                unlockButton.active = canUnlockSkill(selectedSkill);
            }
        }
        
        // 绘制节点背景
        graphics.fill(x, y, x + NODE_SIZE, y + NODE_SIZE, bgColor);
        
        // 绘制节点边框
        graphics.renderOutline(x, y, NODE_SIZE, NODE_SIZE, borderColor);
        
        // 绘制技能图标（如果有）
        if (!node.getSkills().isEmpty()) {
            ISkill skill = node.getSkills().get(0);
            renderSkillIcon(graphics, skill, x + 4, y + 4, NODE_SIZE - 8);
        }
        
        // 绘制层级标记
        graphics.drawCenteredString(this.font, String.valueOf(node.getTier()), 
                x + NODE_SIZE / 2, y + NODE_SIZE + 2, 0xFFFFFF);
    }
    
    /**
     * 渲染技能图标
     */
    private void renderSkillIcon(GuiGraphics graphics, ISkill skill, int x, int y, int size) {
        // 根据技能类型使用不同颜色作为图标
        int color = getSkillTypeColor(skill.getSkillType());
        graphics.fill(x, y, x + size, y + size, color);
        
        // 绘制技能类型首字母
        String letter = skill.getSkillType().name().substring(0, 1);
        graphics.drawCenteredString(this.font, letter, x + size / 2, y + size / 2 - 4, 0xFFFFFF);
    }
    
    /**
     * 获取技能类型颜色
     */
    private int getSkillTypeColor(ISkill.SkillType type) {
        return switch (type) {
            case BASIC_EVOLUTION -> FastColor.ARGB32.color(255, 150, 150, 150);
            case POWER_MUTATION -> FastColor.ARGB32.color(255, 200, 50, 50);
            case AGILITY_MUTATION -> FastColor.ARGB32.color(255, 50, 150, 200);
            case SPECIAL_MUTATION -> FastColor.ARGB32.color(255, 150, 50, 200);
            case DIVINE_ABILITY -> FastColor.ARGB32.color(255, 200, 150, 50);
            case SUPREME_ABILITY -> FastColor.ARGB32.color(255, 200, 200, 50);
        };
    }
    
    /**
     * 渲染技能详情
     */
    private void renderSkillDetails(GuiGraphics graphics) {
        if (selectedSkill == null) return;
        
        int x = 10;
        int y = 30;
        int width = 150;
        
        // 绘制背景
        graphics.fill(x, y, x + width, y + 120, FastColor.ARGB32.color(255, 0, 0, 0));
        graphics.renderOutline(x, y, width, 120, FastColor.ARGB32.color(255, 150, 150, 150));
        
        // 绘制技能名称
        graphics.drawString(this.font, selectedSkill.getName(), x + 5, y + 5, 0xFFFFFF);
        
        // 绘制技能描述
        if (selectedSkill.getDescription() != null) {
            String desc = selectedSkill.getDescription().getString();
            // 简单截断以适应宽度
            if (desc.length() > 40) {
                desc = desc.substring(0, 37) + "...";
            }
            graphics.drawString(this.font, desc, x + 5, y + 20, 0xAAAAAA);
        }
        
        // 绘制消耗
        graphics.drawString(this.font, 
                Component.translatable("gui.corpseorigin.cost", selectedSkill.getCost()), 
                x + 5, y + 40, 0xFFD700);
        
        // 绘制类型
        graphics.drawString(this.font, 
                Component.translatable("gui.corpseorigin.type", selectedSkill.getSkillType().name()), 
                x + 5, y + 55, 0xAAAAAA);
        
        // 绘制解锁状态
        if (isSkillLearned(selectedSkill)) {
            graphics.drawString(this.font, 
                    Component.translatable("gui.corpseorigin.unlocked"), 
                    x + 5, y + 75, 0x00FF00);
        } else if (canUnlockSkill(selectedSkill)) {
            graphics.drawString(this.font, 
                    Component.translatable("gui.corpseorigin.can_unlock"), 
                    x + 5, y + 75, 0xFFFF00);
        } else {
            graphics.drawString(this.font, 
                    Component.translatable("gui.corpseorigin.locked"), 
                    x + 5, y + 75, 0xFF0000);
        }
        
        // 绘制前置技能
        if (!selectedSkill.getPrerequisites().isEmpty()) {
            graphics.drawString(this.font, 
                    Component.translatable("gui.corpseorigin.prerequisites"), 
                    x + 5, y + 90, 0xAAAAAA);
        }
    }
    
    /**
     * 渲染进化点数
     */
    private void renderEvolutionPoints(GuiGraphics graphics) {
        if (skillHandler == null) return;
        
        int points = skillHandler.getEvolutionPoints();
        String text = Component.translatable("gui.corpseorigin.evolution_points", points).getString();
        
        int x = this.width - 120;
        int y = 10;
        
        // 绘制背景
        graphics.fill(x, y, x + 110, y + 25, FastColor.ARGB32.color(255, 0, 0, 0));
        graphics.renderOutline(x, y, 110, 25, FastColor.ARGB32.color(255, 200, 150, 50));
        
        // 绘制文字
        graphics.drawString(this.font, text, x + 5, y + 6, 0xFFD700);
    }
    
    /**
     * 渲染提示
     */
    private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        // 查找鼠标悬停的节点
        if (skillTree == null) return;
        
        for (ISkillNode node : skillTree.getAllNodes()) {
            int x = (int) (scrollX + node.getX() * NODE_SPACING_X) - NODE_SIZE / 2;
            int y = (int) (scrollY + node.getY() * NODE_SPACING_Y) - NODE_SIZE / 2;
            
            if (mouseX >= x && mouseX <= x + NODE_SIZE && 
                mouseY >= y && mouseY <= y + NODE_SIZE) {
                
                if (!node.getSkills().isEmpty()) {
                    ISkill skill = node.getSkills().get(0);
                    List<Component> tooltip = new ArrayList<>();
                    tooltip.add(skill.getName());
                    if (skill.getDescription() != null) {
                        tooltip.add(skill.getDescription());
                    }
                    tooltip.add(Component.translatable("gui.corpseorigin.cost", skill.getCost()));
                    
                    graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
                }
                break;
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 处理拖动开始
        if (button == 0 && skillTree != null) {
            isDragging = true;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            
            // 检查是否点击了节点
            for (ISkillNode node : skillTree.getAllNodes()) {
                int x = (int) (scrollX + node.getX() * NODE_SPACING_X) - NODE_SIZE / 2;
                int y = (int) (scrollY + node.getY() * NODE_SPACING_Y) - NODE_SIZE / 2;
                
                if (mouseX >= x && mouseX <= x + NODE_SIZE && 
                    mouseY >= y && mouseY <= y + NODE_SIZE) {
                    
                    if (!node.getSkills().isEmpty()) {
                        selectedSkill = node.getSkills().get(0);
                        unlockButton.active = canUnlockSkill(selectedSkill);
                        return true;
                    }
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging) {
            scrollX += mouseX - lastMouseX;
            scrollY += mouseY - lastMouseY;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        // 可以添加缩放功能
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }
    
    /**
     * 解锁按钮点击事件
     */
    private void onUnlockButtonClicked(Button button) {
        if (selectedSkill == null || minecraft == null || minecraft.player == null) return;

        if (canUnlockSkill(selectedSkill)) {
            // 发送解锁请求到服务器
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(new SkillUnlockPacket(selectedSkill.getId()));

            // 本地预测更新
            skillHandler.learnSkill(selectedSkill);
            unlockButton.active = false;
        }
    }
    
    /**
     * 检查节点是否已解锁
     */
    private boolean isNodeUnlocked(ISkillNode node) {
        if (skillHandler == null) return false;

        for (ISkill skill : node.getSkills()) {
            if (skillHandler.hasLearned(skill.getId())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查节点是否可以解锁
     */
    private boolean canUnlockNode(ISkillNode node) {
        if (skillHandler == null) return false;
        
        for (ISkill skill : node.getSkills()) {
            if (canUnlockSkill(skill)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 检查技能是否已学习
     */
    private boolean isSkillLearned(ISkill skill) {
        if (skillHandler == null) return false;
        return skillHandler.hasLearned(skill.getId());
    }
    
    /**
     * 检查技能是否可以解锁
     */
    private boolean canUnlockSkill(ISkill skill) {
        if (skillHandler == null) return false;
        if (isSkillLearned(skill)) return false;
        if (skillHandler.getEvolutionPoints() < skill.getCost()) return false;

        // 检查前置技能
        for (ResourceLocation prereq : skill.getPrerequisites()) {
            if (!skillHandler.hasLearned(prereq)) {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose() {
        super.onClose();
    }
    
    /**
     * 显示技能树界面
     */
    public static void show() {
        Minecraft.getInstance().setScreen(new SkillTreeScreen());
    }
}
