package com.phagens.corpseorigin.client.gui.radialmenu;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

/**
 * 轮盘菜单 GUI 渲染类
 * @param <T> 菜单项数据类型
 */
public abstract class GuiRadialMenu<T> extends Screen {
    
    private static final float PRECISION = 5.0f;
    protected static final int MAX_SLOTS = 12;  // 最大槽位数
    
    protected boolean closing = false;
    protected final RadialMenu<T> radialMenu;
    protected final List<IRadialMenuSlot<T>> radialMenuSlots;
    
    // 动画参数
    protected static final float OPEN_ANIMATION_LENGTH = 0.15f;
    protected float totalTime = 0;
    protected int selectedItem = -1;
    
    // 半径参数
    protected static final float INNER_RADIUS = 30;
    protected static final float OUTER_RADIUS = 100;
    protected static final float ANIMATED_RADIUS = 120;
    
    // 颜色
    protected static final int COLOR_BACKGROUND = 0xFF2A2A3E;
    protected static final int COLOR_HIGHLIGHT = 0xFF4488FF;
    protected static final int COLOR_SELECTED = 0xFF66AAFF;
    protected static final int COLOR_COOLDOWN = 0xFFAA4444;
    protected static final int COLOR_DISABLED = 0xFF444444;
    
    protected final Minecraft minecraft;
    protected int centerX;
    protected int centerY;
    
    public GuiRadialMenu(RadialMenu<T> radialMenu) {
        super(Component.empty());
        this.radialMenu = radialMenu;
        this.radialMenuSlots = radialMenu.getSlots();
        this.minecraft = Minecraft.getInstance();
    }
    
    @Override
    protected void init() {
        super.init();
        centerX = this.width / 2;
        centerY = this.height / 2;
    }
    
    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 渲染半透明黑色背景
        renderBackground(graphics);
        
        // 更新动画时间
        totalTime += partialTicks / 20f;
        
        // 计算动画进度
        float animProgress = Math.min(1.0f, totalTime / OPEN_ANIMATION_LENGTH);
        animProgress = (float) (1 - Math.pow(1 - animProgress, 3)); // 缓动函数
        
        // 计算当前半径
        float radiusIn = INNER_RADIUS * animProgress;
        float radiusOut = OUTER_RADIUS * animProgress;
        
        // 计算鼠标位置和选中项
        updateSelectedItem(mouseX, mouseY, radiusIn, radiusOut);
        
        // 渲染轮盘扇区
        renderSlices(graphics, radiusIn, radiusOut);
        
        // 渲染中心
        renderCenter(graphics, radiusIn);
        
        // 渲染选中项信息
        if (selectedItem != -1 && selectedItem < radialMenuSlots.size()) {
            renderSelectedInfo(graphics);
        }
        
        super.render(graphics, mouseX, mouseY, partialTicks);
    }
    
    /**
     * 渲染背景 - 深色不透明背景
     */
    protected void renderBackground(GuiGraphics graphics) {
        // 填充整个屏幕的深色不透明背景
        graphics.fill(0, 0, this.width, this.height, 0xFF1A1A2E);
    }
    
    /**
     * 更新选中的项目
     */
    protected void updateSelectedItem(int mouseX, int mouseY, float radiusIn, float radiusOut) {
        if (closing) {
            selectedItem = -1;
            return;
        }
        
        // 计算鼠标相对于中心的角度和距离
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        double angle = Math.toDegrees(Math.atan2(dy, dx));
        
        // 标准化角度到 0-360
        angle = (angle + 360) % 360;
        
        // 检查是否在轮盘范围内
        if (distance < radiusIn || distance > radiusOut) {
            selectedItem = -1;
            return;
        }
        
        // 计算选中的扇区
        int slotCount = radialMenuSlots.size();
        if (slotCount == 0) {
            selectedItem = -1;
            return;
        }
        
        // 每个扇区的角度
        float anglePerSlot = 360f / slotCount;
        
        // 调整角度，使第一个扇区从顶部开始
        double adjustedAngle = (angle - 90 + 360) % 360;
        
        // 计算选中的索引
        selectedItem = (int) (adjustedAngle / anglePerSlot);
        if (selectedItem >= slotCount) {
            selectedItem = slotCount - 1;
        }
    }
    
    /**
     * 渲染轮盘扇区
     */
    protected void renderSlices(GuiGraphics graphics, float radiusIn, float radiusOut) {
        int slotCount = radialMenuSlots.size();
        if (slotCount == 0) return;
        
        float anglePerSlot = 360f / slotCount;
        
        for (int i = 0; i < slotCount; i++) {
            float startAngle = i * anglePerSlot - 90 - anglePerSlot / 2;
            float endAngle = startAngle + anglePerSlot;
            
            boolean isSelected = (i == selectedItem);
            boolean isHighlighted = isSelected;
            
            // 获取槽位
            IRadialMenuSlot<T> slot = radialMenuSlots.get(i);
            
            // 绘制扇区
            drawSlice(graphics, slot, isHighlighted, centerX, centerY, 0, 
                    radiusIn, radiusOut, startAngle, endAngle, i);
            
            // 绘制图标
            drawSlotIcon(graphics, slot, i, slotCount, radiusIn, radiusOut);
        }
    }
    
    /**
     * 绘制单个扇区
     */
    public void drawSlice(GuiGraphics graphics, IRadialMenuSlot<T> slot, boolean highlighted,
                          float x, float y, float z, float radiusIn, float radiusOut,
                          float startAngle, float endAngle, int index) {
        
        float angle = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(angle / PRECISION));
        
        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;
        
        // 确定颜色
        int color = getSliceColor(slot, highlighted, index);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int a = (color >> 24) & 0xFF;
        
        // 使用 RenderType.gui() 获取 buffer
        var bufferSource = graphics.bufferSource();
        var buffer = bufferSource.getBuffer(RenderType.gui());
        Matrix4f matrix = graphics.pose().last().pose();
        
        // 绘制三角形扇
        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;
            
            float x1 = x + radiusIn * (float) Math.cos(angle1);
            float y1 = y + radiusIn * (float) Math.sin(angle1);
            float x2 = x + radiusOut * (float) Math.cos(angle1);
            float y2 = y + radiusOut * (float) Math.sin(angle1);
            float x3 = x + radiusOut * (float) Math.cos(angle2);
            float y3 = y + radiusOut * (float) Math.sin(angle2);
            float x4 = x + radiusIn * (float) Math.cos(angle2);
            float y4 = y + radiusIn * (float) Math.sin(angle2);
            
            // 添加四个顶点（两个三角形）
            buffer.addVertex(matrix, x2, y2, z).setColor(r, g, b, a);
            buffer.addVertex(matrix, x1, y1, z).setColor(r, g, b, a);
            buffer.addVertex(matrix, x4, y4, z).setColor(r, g, b, a);
            buffer.addVertex(matrix, x3, y3, z).setColor(r, g, b, a);
        }
    }
    
    /**
     * 获取扇区颜色
     */
    protected int getSliceColor(IRadialMenuSlot<T> slot, boolean highlighted, int index) {
        if (highlighted) {
            return COLOR_HIGHLIGHT;
        }
        return COLOR_BACKGROUND;
    }
    
    /**
     * 绘制槽位图标
     */
    protected void drawSlotIcon(GuiGraphics graphics, IRadialMenuSlot<T> slot, 
                                 int index, int totalSlots, float radiusIn, float radiusOut) {
        float anglePerSlot = 360f / totalSlots;
        float angle = index * anglePerSlot - 90;
        float radians = (float) Math.toRadians(angle);
        
        // 计算图标位置（扇区中心）
        float iconRadius = (radiusIn + radiusOut) / 2;
        int iconX = (int) (centerX + iconRadius * Math.cos(radians));
        int iconY = (int) (centerY + iconRadius * Math.sin(radians));
        
        // 绘制主图标
        drawIcon(graphics, slot.primarySlotIcon(), iconX, iconY);
        
        // 绘制次要图标
        if (slot.hasSecondaryIcons()) {
            List<T> secondaryIcons = slot.secondarySlotIcons();
            for (int i = 0; i < secondaryIcons.size() && i < 3; i++) {
                float offsetAngle = radians + (i - 1) * 0.3f;
                int secX = (int) (centerX + iconRadius * 0.7 * Math.cos(offsetAngle));
                int secY = (int) (centerY + iconRadius * 0.7 * Math.sin(offsetAngle));
                drawSecondaryIcon(graphics, secondaryIcons.get(i), secX, secY);
            }
        }
    }
    
    /**
     * 绘制图标（子类实现）
     */
    protected abstract void drawIcon(GuiGraphics graphics, T icon, int x, int y);
    
    /**
     * 绘制次要图标（子类实现）
     */
    protected abstract void drawSecondaryIcon(GuiGraphics graphics, T icon, int x, int y);
    
    /**
     * 渲染中心
     */
    protected void renderCenter(GuiGraphics graphics, float radius) {
        // 绘制中心圆
        graphics.fill(centerX - (int) radius, centerY - (int) radius,
                centerX + (int) radius, centerY + (int) radius, COLOR_BACKGROUND);
        
        // 绘制中心文字
        if (selectedItem != -1 && selectedItem < radialMenuSlots.size()) {
            IRadialMenuSlot<T> slot = radialMenuSlots.get(selectedItem);
            Component name = slot.getName();
            int textWidth = minecraft.font.width(name);
            graphics.drawString(minecraft.font, name, 
                    centerX - textWidth / 2, centerY - 4, 0xFFFFFF, true);
        }
    }
    
    /**
     * 渲染选中项信息
     */
    protected void renderSelectedInfo(GuiGraphics graphics) {
        // 在屏幕底部显示详细信息
        if (selectedItem >= 0 && selectedItem < radialMenuSlots.size()) {
            IRadialMenuSlot<T> slot = radialMenuSlots.get(selectedItem);
            Component name = slot.getName();
            
            int textWidth = minecraft.font.width(name);
            int x = (this.width - textWidth) / 2;
            int y = this.height - 40;
            
            // 绘制背景
            graphics.fill(x - 5, y - 5, x + textWidth + 5, y + 15, 0xFF000000);
            
            // 绘制文字
            graphics.drawString(minecraft.font, name, x, y, 0xFFFFFF, true);
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1) { // 右键关闭
            this.onClose();
            return true;
        } else if (button == 0) { // 左键选择
            if (selectedItem != -1) {
                radialMenu.select(selectedItem);
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 数字键快捷选择
        int number = keyCode - 48; // '0' 是 48
        if (number >= 1 && number <= radialMenuSlots.size()) {
            selectedItem = number - 1;
            radialMenu.select(selectedItem);
            this.onClose();
            return true;
        }
        
        // ESC关闭
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public void onClose() {
        closing = true;
        super.onClose();
    }
}
