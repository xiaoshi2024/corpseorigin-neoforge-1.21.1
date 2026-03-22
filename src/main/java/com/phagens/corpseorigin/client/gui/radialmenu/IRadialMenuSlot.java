package com.phagens.corpseorigin.client.gui.radialmenu;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 轮盘菜单槽位接口 - 定义轮盘菜单中每个槽位的数据结构
 *
 * 【功能说明】
 * 1. 定义槽位的显示名称
 * 2. 定义主图标数据（必需）
 * 3. 定义次要图标列表（可选）
 * 4. 支持复合槽位（多个图标）
 *
 * 【使用场景】
 * - 技能槽位：主图标=技能，次要图标=冷却指示
 * - 物品槽位：主图标=物品，次要图标=数量/耐久
 * - 菜单槽位：主图标=菜单项，次要图标=快捷键
 *
 * 【实现类】
 * - RadialMenuSlot: 基础实现类
 *
 * @param <T> 槽位中存储的数据类型
 * @author Phagens
 * @version 1.0
 */
public interface IRadialMenuSlot<T> {
    
    /**
     * 获取槽位显示名称
     */
    Component getName();
    
    /**
     * 获取主槽位图标（数据）
     */
    T primarySlotIcon();
    
    /**
     * 获取次要槽位图标列表
     */
    List<T> secondarySlotIcons();
    
    /**
     * 检查是否有次要图标
     */
    default boolean hasSecondaryIcons() {
        return !secondarySlotIcons().isEmpty();
    }
}
