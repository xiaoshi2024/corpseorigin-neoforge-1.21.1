package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.event.custom.WeaponBreakEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Point Blank 枪械模组事件处理器
 * 监听枪械射击事件并触发 WeaponBreakEvent
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class PointBlankGunEventHandler {

    // Point Blank 命名空间
    private static final String POINT_BLANK_NAMESPACE = "pointblank";

    // 记录玩家射击冷却
    private static final Map<UUID, Integer> playerShootCooldown = new HashMap<>();
    // 记录玩家上次造成伤害的时间
    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();

    /**
     * 检测物品是否为 Point Blank 枪械
     */
    public static boolean isPointBlankGun(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 方法1: 获取物品的注册表ID (如 pointblank:ak47)
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (registryName != null) {
            // 检查命名空间是否为 pointblank
            if (POINT_BLANK_NAMESPACE.equals(registryName.getNamespace())) {
                return true;
            }
        }

        // 方法2: 检查物品描述ID (如 item.pointblank.ak47)
        String descriptionId = stack.getItem().getDescriptionId();
        if (descriptionId != null && descriptionId.toLowerCase().contains(POINT_BLANK_NAMESPACE)) {
            return true;
        }

        // 方法3: 检查物品的类名
        String className = stack.getItem().getClass().getName().toLowerCase();
        if (className.contains("gun") || className.contains("firearm") || className.contains("weapon")) {
            return true;
        }

        return false;
    }

    /**
     * 监听伤害事件 - Point Blank 射击造成伤害时触发
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        // 获取伤害来源
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        ItemStack mainHand = player.getMainHandItem();

        // 检查是否使用 Point Blank 枪械造成伤害
        if (isPointBlankGun(mainHand)) {
            CorpseOrigin.LOGGER.info("[PointBlank检测] 玩家使用枪械造成伤害，准备触发 WeaponBreakEvent");

            UUID playerId = player.getUUID();
            long currentTime = System.currentTimeMillis();

            // 检查冷却 (500ms 内不重复触发)
            Long lastTime = lastDamageTime.get(playerId);
            if (lastTime == null || currentTime - lastTime > 500) {
                // 触发 WeaponBreakEvent
                WeaponBreakEvent weaponBreakEvent = new WeaponBreakEvent(player, EquipmentSlot.MAINHAND);
                weaponBreakEvent.setDurabilityZero(true); // 直接移除

                NeoForge.EVENT_BUS.post(weaponBreakEvent);

                CorpseOrigin.LOGGER.info("[PointBlank检测] WeaponBreakEvent 已触发，枪械将被移除");

                // 记录上次触发时间
                lastDamageTime.put(playerId, currentTime);
            } else {
                CorpseOrigin.LOGGER.info("[PointBlank检测] 冷却中，跳过触发");
            }
        }
    }

    /**
     * 玩家tick事件 - 清理冷却
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        UUID playerId = player.getUUID();

        // 检查射击冷却
        int cooldown = playerShootCooldown.getOrDefault(playerId, 0);
        if (cooldown > 0) {
            playerShootCooldown.put(playerId, cooldown - 1);
        }
    }

    /**
     * 监听右键点击事件（备用方案）
     */
    @SubscribeEvent
    public static void onPlayerRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (player.level().isClientSide()) return;

        if (isPointBlankGun(stack)) {
            CorpseOrigin.LOGGER.info("[PointBlank检测] 右键点击枪械");
        }
    }
}
