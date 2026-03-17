package com.phagens.corpseorigin.client.gui.radialmenu;

import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 轮盘菜单槽位接口
 * @param <T> 槽位中存储的数据类型
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
