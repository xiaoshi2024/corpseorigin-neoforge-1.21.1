/**
 * 武器损坏事件 - 自定义事件，用于处理武器耐久耗尽逻辑
 *
 * 【功能说明】
 * 1. 在武器耐久即将耗尽时触发
 * 2. 支持取消事件（阻止默认处理）
 * 3. 支持自定义耐久消耗值
 * 4. 支持强制耐久归零选项
 *
 * 【事件属性】
 * - attacker: 武器持有者
 * - slot: 装备槽位（主手/副手）
 * - isDurabilityZero: 是否强制将耐久设为0
 * - customDamageAmount: 自定义耐久消耗值
 *
 * 【使用场景】
 * - 自定义武器损坏逻辑
 * - 特殊武器（如Point Blank枪械）的处理
 * - 防止某些武器损坏
 *
 * 【取消机制】
 * 实现ICancellableEvent接口，支持事件取消
 * 取消后isDurabilityZero设为false
 *
 * 【关联系统】
 * - WeaponBreakEventHandler: 事件处理器
 * - PointBlankGunEventHandler: 枪械特殊处理
 *
 * @author Phagens
 * @version 1.0
 */
package com.phagens.corpseorigin.event.custom;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 武器损坏事件
 * 继承Event并实现ICancellableEvent接口以支持取消
 */
public class WeaponBreakEvent extends Event implements ICancellableEvent {

    /** 武器持有者 */
    private final LivingEntity attacker;
    /** 装备槽位 */
    private final EquipmentSlot slot;
    /** 是否强制耐久归零 */
    private boolean isDurabilityZero = true;
    /** 自定义耐久消耗值 */
    private int customDamageAmount = 0;

    /**
     * 构造函数
     *
     * @param attacker 武器持有者
     * @param slot 装备槽位
     */
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