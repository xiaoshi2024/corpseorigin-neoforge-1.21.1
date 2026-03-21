package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.event.custom.WeaponBreakEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerDestroyItemEvent;

// NeoForge 事件注册
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class WeaponBreakEventHandler {

    // 订阅自定义事件
    @SubscribeEvent
    public static void onWeaponBreak(WeaponBreakEvent event) {
        // 1. 检查事件是否被取消
        if (event.isCanceled()) {
            return;
        }

        LivingEntity attacker = event.getAttacker();
        EquipmentSlot slot = event.getSlot();
        ItemStack weaponStack = attacker.getItemBySlot(slot);

        // 2. 空指针 + 客户端防护（仅服务端处理）
        if (weaponStack.isEmpty() || attacker.level().isClientSide()) {
            return;
        }

        // 3. 耐久处理逻辑
        if (event.isDurabilityZero()) {
            // 强制耐久归零
            int maxDamage = weaponStack.getMaxDamage();
            weaponStack.setDamageValue(Math.min(maxDamage, weaponStack.getDamageValue() + maxDamage));
        } else if (event.getCustomDamageAmount() > 0) {
            // 自定义耐久消耗
            weaponStack.hurtAndBreak(event.getCustomDamageAmount(), attacker, 
                    slot);
        }

        // 4. 耐久耗尽 → 装备破碎
        if (weaponStack.getDamageValue() >= weaponStack.getMaxDamage()) {
            if (attacker instanceof ServerPlayer serverPlayer) {
                // 发送装备破碎动画包（29 = BREAK_EQUIPMENT_SLOT）
                serverPlayer.connection.send(new ClientboundAnimatePacket(serverPlayer, 29 + slot.getIndex()));

                // 播放破碎音效
                serverPlayer.level().playSound(
                        null,
                        serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                        SoundEvents.ITEM_BREAK,
                        SoundSource.PLAYERS,
                        1.0F, 1.0F
                );

                // NeoForge 内置事件：触发玩家销毁物品事件
                // 将 EquipmentSlot 转换为 InteractionHand
                InteractionHand hand = (slot == EquipmentSlot.MAINHAND) ? InteractionHand.MAIN_HAND : 
                                      (slot == EquipmentSlot.OFFHAND) ? InteractionHand.OFF_HAND : null;
                if (hand != null) {
                    NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(serverPlayer, weaponStack, hand));
                }
            }

            // 最终：清空装备槽
            attacker.setItemSlot(slot, ItemStack.EMPTY);
        }
    }
}
