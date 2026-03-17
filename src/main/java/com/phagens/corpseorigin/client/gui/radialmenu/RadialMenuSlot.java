package com.phagens.corpseorigin.client.gui.radialmenu;

import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

/**
 * 轮盘菜单槽位实现
 * @param <T> 槽位中存储的数据类型
 */
public record RadialMenuSlot<T>(Component name, T primarySlotIcon, List<T> secondarySlotIcons) 
        implements IRadialMenuSlot<T> {
    
    /**
     * 创建只有主图标的槽位
     */
    public RadialMenuSlot(Component name, T primarySlotIcon) {
        this(name, primarySlotIcon, Collections.emptyList());
    }
    
    @Override
    public Component getName() {
        return name;
    }
    
    @Override
    public T primarySlotIcon() {
        return primarySlotIcon;
    }
    
    @Override
    public List<T> secondarySlotIcons() {
        return secondarySlotIcons;
    }
}
