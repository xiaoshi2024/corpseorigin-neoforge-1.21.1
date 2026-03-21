package com.phagens.corpseorigin.event.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

// 关键：实现 ICancellableEvent 接口（替代 @Cancelable 注解）
public class WeaponBreakEvent extends Event implements ICancellableEvent {
    // 事件核心数据
    private final LivingEntity attacker;
    private final EquipmentSlot slot;
    private boolean isDurabilityZero = true; // 是否强制耐久归零
    private int customDamageAmount = 0;     // 自定义耐久消耗值（可选）

    // 构造方法
    public WeaponBreakEvent(LivingEntity attacker, EquipmentSlot slot) {
        this.attacker = attacker;
        this.slot = slot;
    }

    // ========== ICancellableEvent 接口实现（可选重写，默认直接用接口默认方法即可） ==========
    // 如需监听取消事件，可重写此方法（必须调用 super）
    @Override
    @MustBeInvokedByOverriders
    public void setCanceled(boolean canceled) {
        // 调用接口默认实现（核心：修改 Event 内部的 isCanceled 字段）
        ICancellableEvent.super.setCanceled(canceled);
        // 可选：添加自定义取消逻辑（如日志、额外状态重置）
        if (canceled) {
            this.isDurabilityZero = false; // 取消时关闭耐久归零
        }
    }

    // ========== Getter/Setter 方法 ==========
    public LivingEntity getAttacker() {
        return attacker;
    }

    public EquipmentSlot getSlot() {
        return slot;
    }

    public boolean isDurabilityZero() {
        return isDurabilityZero;
    }

    public void setDurabilityZero(boolean durabilityZero) {
        this.isDurabilityZero = durabilityZero;
    }

    public int getCustomDamageAmount() {
        return customDamageAmount;
    }

    public void setCustomDamageAmount(int customDamageAmount) {
        this.customDamageAmount = customDamageAmount;
    }
}