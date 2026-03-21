package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.event.custom.WeaponBreakEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

    // Point Blank 命名空间
    private static final String POINT_BLANK_NAMESPACE = "pointblank";

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

        // 3. 检查是否为 Point Blank 枪械（直接移除）
        if (isPointBlankGun(weaponStack)) {
            removeGunAndNotify(attacker, slot, weaponStack);
            return;
        }

        // 4. 普通武器耐久处理逻辑
        if (event.isDurabilityZero()) {
            // 强制耐久归零
            int maxDamage = weaponStack.getMaxDamage();
            weaponStack.setDamageValue(Math.min(maxDamage, weaponStack.getDamageValue() + maxDamage));
        } else if (event.getCustomDamageAmount() > 0) {
            // 自定义耐久消耗
            weaponStack.hurtAndBreak(event.getCustomDamageAmount(), attacker, slot);
        }

        // 5. 耐久耗尽 → 装备破碎
        if (weaponStack.getDamageValue() >= weaponStack.getMaxDamage()) {
            breakWeapon(attacker, slot, weaponStack);
        }
    }

    /**
     * 检测物品是否为 Point Blank 枪械
     */
    private static boolean isPointBlankGun(ItemStack stack) {
        if (stack.isEmpty()) return false;

        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (registryName != null) {
            return POINT_BLANK_NAMESPACE.equals(registryName.getNamespace());
        }
        return false;
    }

    /**
     * 移除枪械并通知玩家
     */
    private static void removeGunAndNotify(LivingEntity attacker, EquipmentSlot slot, ItemStack weaponStack) {
        if (attacker instanceof ServerPlayer serverPlayer) {
            // 播放破碎音效
            serverPlayer.level().playSound(
                    null,
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.ITEM_BREAK,
                    SoundSource.PLAYERS,
                    1.0F, 1.0F
            );

            // 发送移除消息
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c你的 " + weaponStack.getHoverName().getString() + " 已损坏并被移除！"));

            // NeoForge 内置事件：触发玩家销毁物品事件
            InteractionHand hand = (slot == EquipmentSlot.MAINHAND) ? InteractionHand.MAIN_HAND :
                                  (slot == EquipmentSlot.OFFHAND) ? InteractionHand.OFF_HAND : null;
            if (hand != null) {
                NeoForge.EVENT_BUS.post(new PlayerDestroyItemEvent(serverPlayer, weaponStack, hand));
            }
        }

        // 清空装备槽
        attacker.setItemSlot(slot, ItemStack.EMPTY);
    }

    /**
     * 武器破碎处理（普通武器）
     */
    private static void breakWeapon(LivingEntity attacker, EquipmentSlot slot, ItemStack weaponStack) {
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

            // 发送破碎消息
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§c你的 " + weaponStack.getHoverName().getString() + " 损坏了！"));

            // NeoForge 内置事件：触发玩家销毁物品事件
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
