package com.phagens.corpseorigin.client.gui.radialmenu;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 轮盘菜单数据模型
 * @param <T> 菜单项数据类型
 */
public class RadialMenu<T> {
    
    private final Consumer<Integer> onSelect;
    private final List<IRadialMenuSlot<T>> slots;
    private final Function<T, Void> drawCallback;
    private int currentSlot;
    
    /**
     * 创建轮盘菜单
     * @param onSelect 选择回调
     * @param slots 槽位列表
     * @param drawCallback 绘制回调
     * @param currentSlot 当前选中的槽位
     */
    public RadialMenu(Consumer<Integer> onSelect, List<IRadialMenuSlot<T>> slots, 
                      Function<T, Void> drawCallback, int currentSlot) {
        this.onSelect = onSelect;
        this.slots = slots;
        this.drawCallback = drawCallback;
        this.currentSlot = currentSlot;
    }
    
    /**
     * 获取槽位数量
     */
    public int getSlotCount() {
        return slots.size();
    }
    
    /**
     * 获取所有槽位
     */
    public List<IRadialMenuSlot<T>> getSlots() {
        return slots;
    }
    
    /**
     * 获取指定槽位
     */
    public IRadialMenuSlot<T> getSlot(int index) {
        if (index < 0 || index >= slots.size()) {
            return null;
        }
        return slots.get(index);
    }
    
    /**
     * 获取当前选中的槽位索引
     */
    public int getCurrentSlot() {
        return currentSlot;
    }
    
    /**
     * 设置当前选中的槽位
     */
    public void setCurrentSlot(int slot) {
        this.currentSlot = slot;
        if (onSelect != null) {
            onSelect.accept(slot);
        }
    }
    
    /**
     * 触发选择
     */
    public void select(int slot) {
        setCurrentSlot(slot);
    }
    
    /**
     * 获取绘制回调
     */
    public Function<T, Void> getDrawCallback() {
        return drawCallback;
    }
}
