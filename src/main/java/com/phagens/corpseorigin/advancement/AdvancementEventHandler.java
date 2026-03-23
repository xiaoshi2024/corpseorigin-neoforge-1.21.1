package com.phagens.corpseorigin.advancement;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.entity.LongyouEntity;
import com.phagens.corpseorigin.entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.event.custom.WeaponBreakEvent;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * 原版成就事件处理器 - 监听游戏事件并触发原版成就
 * 
 * 【功能说明】
 * 1. 监听玩家变成尸兄的事件 -> 触发 become_corpse 成就
 * 2. 监听玩家遇见尸王的事件 -> 触发 meet_corpse_king 成就
 * 3. 监听武器被尸王震坏的事件 -> 触发 weapon_shattered 成就
 * 4. 监听尸兄互相吞噬的事件 -> 触发 cannibalism_discovery 成就
 * 
 * @author Phagens
 * @version 1.0
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class AdvancementEventHandler {
    
    /** 检测尸王的范围 */
    private static final double CORPSE_KING_RANGE = 50.0;
    /** 检测尸兄吞噬的范围 */
    private static final double CANNIBALISM_RANGE = 30.0;
    
    /**
     * 玩家tick事件 - 周期性检查成就条件
     * 每20tick（1秒）检查一次
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // 只在服务端处理
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        
        // 每20tick检查一次（约1秒）
        if (player.tickCount % 20 != 0) {
            return;
        }
        
        // 检查"成为尸兄"成就
        if (PlayerCorpseData.isCorpse(player)) {
            CriterionTriggerRegister.BECOME_CORPSE.get().trigger(serverPlayer);
        }
        
        // 检查"初见尸王"成就
        if (isCorpseKingNearby(serverPlayer)) {
            CriterionTriggerRegister.MEET_CORPSE_KING.get().trigger(serverPlayer);
        }
    }
    
    /**
     * 武器损坏事件 - 检查是否由尸王造成
     */
    @SubscribeEvent
    public static void onWeaponBreak(WeaponBreakEvent event) {
        LivingEntity attacker = event.getAttacker();
        
        // 检查攻击者是否为玩家
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }
        
        // 检查玩家附近是否有尸王
        if (!isCorpseKingNearby(player)) {
            return;
        }
        
        // 触发"相鼠有皮"成就
        CriterionTriggerRegister.WEAPON_SHATTERED.get().trigger(player);
    }
    
    /**
     * 实体死亡事件 - 检查是否为尸兄互相吞噬
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        
        // 检查受害者是否为尸兄
        if (!isCorpseBrotherEntity(victim)) {
            return;
        }
        
        // 获取击杀者
        Entity killer = source.getEntity();
        if (!(killer instanceof LivingEntity livingKiller)) {
            return;
        }
        
        // 检查击杀者是否也是尸兄
        if (!isCorpseBrotherEntity(livingKiller)) {
            return;
        }
        
        // 找到附近的玩家并触发成就
        triggerCannibalismAchievement(victim.level(), victim.position());
    }
    
    /**
     * 检查玩家附近是否有尸王
     */
    private static boolean isCorpseKingNearby(ServerPlayer player) {
        Vec3 playerPos = player.position();
        AABB searchBox = new AABB(
            playerPos.x - CORPSE_KING_RANGE, playerPos.y - CORPSE_KING_RANGE, playerPos.z - CORPSE_KING_RANGE,
            playerPos.x + CORPSE_KING_RANGE, playerPos.y + CORPSE_KING_RANGE, playerPos.z + CORPSE_KING_RANGE
        );
        
        List<Entity> nearbyEntities = player.level().getEntities(player, searchBox, entity -> {
            return entity.getType() == EntityRegistry.LONGYOU.get() && 
                   entity instanceof LivingEntity living && living.isAlive();
        });
        
        return !nearbyEntities.isEmpty();
    }
    
    /**
     * 检查实体是否为尸兄
     */
    private static boolean isCorpseBrotherEntity(LivingEntity entity) {
        // 检查是否为低级尸兄
        if (entity instanceof LowerLevelZbEntity) {
            return true;
        }
        
        // 检查是否为尸王（龙右）
        if (entity instanceof LongyouEntity) {
            return true;
        }
        
        // 检查是否为尸兄鱼
        if (entity.getType() == EntityRegistry.ZBR_FISH.get()) {
            return true;
        }
        
        // 检查玩家是否为尸兄
        if (entity instanceof Player player) {
            return PlayerCorpseData.isCorpse(player);
        }
        
        return false;
    }
    
    /**
     * 触发吞噬成就
     */
    private static void triggerCannibalismAchievement(net.minecraft.world.level.Level level, Vec3 position) {
        // 获取范围内的所有玩家
        AABB searchBox = new AABB(
            position.x - CANNIBALISM_RANGE, position.y - CANNIBALISM_RANGE, position.z - CANNIBALISM_RANGE,
            position.x + CANNIBALISM_RANGE, position.y + CANNIBALISM_RANGE, position.z + CANNIBALISM_RANGE
        );
        
        List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, searchBox, player -> {
            return player instanceof ServerPlayer;
        });
        
        for (Player player : nearbyPlayers) {
            if (player instanceof ServerPlayer serverPlayer) {
                // 触发"吞噬本能"成就
                CriterionTriggerRegister.CANNIBALISM_DISCOVERY.get().trigger(serverPlayer);
            }
        }
    }
}
