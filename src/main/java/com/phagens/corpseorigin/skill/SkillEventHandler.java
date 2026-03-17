package com.phagens.corpseorigin.skill;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 技能事件处理器 - 处理被动技能效果
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class SkillEventHandler {
    
    // 存储玩家对生物造成的伤害，用于计算进化点数
    private static final Map<UUID, Map<UUID, Float>> damageTracker = new HashMap<>();
    
    // 生物类型对应的进化点数
    private static final Map<MobCategory, Integer> EVOLUTION_POINTS_MAP = new HashMap<>();
    
    static {
        // 初始化进化点数映射
        EVOLUTION_POINTS_MAP.put(MobCategory.CREATURE, 1);      // 动物：1点
        EVOLUTION_POINTS_MAP.put(MobCategory.AMBIENT, 1);       // 环境生物：1点
        EVOLUTION_POINTS_MAP.put(MobCategory.MONSTER, 2);       // 怪物：2点
        EVOLUTION_POINTS_MAP.put(MobCategory.AXOLOTLS, 1);      // 美西螈：1点
        EVOLUTION_POINTS_MAP.put(MobCategory.UNDERGROUND_WATER_CREATURE, 2); // 水下生物：2点
        EVOLUTION_POINTS_MAP.put(MobCategory.WATER_CREATURE, 2); // 水生生物：2点
        EVOLUTION_POINTS_MAP.put(MobCategory.WATER_AMBIENT, 1);  // 水环境生物：1点
    }
    
    /**
     * 玩家tick事件 - 处理持续性的被动技能效果
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        if (!PlayerCorpseData.isCorpse(player)) {
            return;
        }
        
        ISkillHandler handler = SkillAttachment.getSkillHandler(player);
        
        // 更新技能冷却
        if (handler instanceof SkillHandler skillHandler) {
            skillHandler.updateCooldowns();
        }
        
        // 每20tick（1秒）处理一次被动效果
        if (player.tickCount % 20 == 0) {
            // 进化感知 - 夜视效果
            if (handler.hasLearned(CorpseSkills.EVOLUTION_SENSE.getId())) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
            }
            
            // 快速再生 - 生命恢复
            if (handler.hasLearned(CorpseSkills.REGENERATION.getId())) {
                if (player.getHealth() < player.getMaxHealth()) {
                    player.heal(0.5f);
                }
            }
        }
    }
    
    /**
     * 玩家攻击事件 - 处理攻击相关的被动技能，并追踪伤害
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof Player player) {
            if (!PlayerCorpseData.isCorpse(player)) {
                return;
            }
            
            ISkillHandler handler = SkillAttachment.getSkillHandler(player);
            LivingEntity target = event.getEntity();
            
            // 追踪玩家对生物造成的伤害
            if (isLivingCreature(target)) {
                trackDamage(player, target, event.getNewDamage());
            }
            
            // 毒液 - 攻击带毒
            if (handler.hasLearned(CorpseSkills.VENOM.getId())) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
            }
            
            // 重击 - 击退效果
            if (handler.hasLearned(CorpseSkills.HEAVY_STRIKE.getId())) {
                double knockbackX = target.getX() - player.getX();
                double knockbackZ = target.getZ() - player.getZ();
                double knockbackStrength = 0.8;
                
                target.knockback(knockbackStrength, knockbackX, knockbackZ);
            }
            
            // 吞噬强化 - 额外恢复
            if (handler.hasLearned(CorpseSkills.DEVOUR_ENHANCEMENT.getId())) {
                if (isLivingCreature(target)) {
                    float healAmount = 1.5f;
                    player.heal(healAmount);
                    
                    // 增加饥饿度
                    int currentHunger = PlayerCorpseData.getHunger(player);
                    PlayerCorpseData.setHunger(player, Math.min(100, currentHunger + 8));
                }
            }
        }
        
        // 闪避 - 受到伤害时有几率闪避
        if (event.getEntity() instanceof Player player) {
            if (!PlayerCorpseData.isCorpse(player)) {
                return;
            }
            
            ISkillHandler handler = SkillAttachment.getSkillHandler(player);
            
            if (handler.hasLearned(CorpseSkills.EVASION.getId())) {
                // 25%几率闪避
                if (player.getRandom().nextFloat() < 0.25f) {
                    // 取消伤害（通过恢复生命值）
                    player.heal(event.getNewDamage());
                    // 播放闪避效果
                    player.level().broadcastEntityEvent(player, (byte) 29); // 盾牌阻挡效果
                }
            }
        }
    }
    
    /**
     * 玩家登录事件 - 同步技能数据
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        
        if (PlayerCorpseData.isCorpse(player)) {
            ISkillHandler handler = SkillAttachment.getSkillHandler(player);
            handler.syncToClient();
            
            CorpseOrigin.LOGGER.debug("同步玩家 {} 的技能数据到客户端", player.getName().getString());
        }
    }
    
    /**
     * 玩家重生事件 - 重新应用被动技能效果
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        
        if (PlayerCorpseData.isCorpse(player)) {
            ISkillHandler handler = SkillAttachment.getSkillHandler(player);
            
            // 重新应用所有被动技能
            for (ISkill skill : handler.getLearnedSkills()) {
                if (skill.isPassive()) {
                    skill.onLearn(player);
                }
            }
            
            handler.syncToClient();
        }
    }
    
    /**
     * 玩家克隆事件 - 复制技能数据
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        if (PlayerCorpseData.isCorpse(original)) {
            ISkillHandler originalHandler = SkillAttachment.getSkillHandler(original);
            ISkillHandler newHandler = SkillAttachment.getSkillHandler(newPlayer);
            
            // 复制进化点数
            newHandler.setEvolutionPoints(originalHandler.getEvolutionPoints());
            
            // 重新学习所有技能
            for (ISkill skill : originalHandler.getLearnedSkills()) {
                newHandler.learnSkill(skill);
            }
            
            CorpseOrigin.LOGGER.debug("复制玩家 {} 的技能数据到新实体", newPlayer.getName().getString());
        }
    }
    
    /**
     * 生物死亡事件 - 给予进化点数
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 检查伤害来源是否是玩家
        if (event.getSource() == null || !(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        
        // 检查玩家是否是尸兄
        if (!PlayerCorpseData.isCorpse(player)) {
            CorpseOrigin.LOGGER.debug("玩家 {} 不是尸兄，无法获得进化点", player.getName().getString());
            return;
        }
        
        LivingEntity target = event.getEntity();
        
        // 检查是否是活物
        if (!isLivingCreature(target)) {
            CorpseOrigin.LOGGER.debug("目标 {} 不是可吞噬的生物", target.getName().getString());
            return;
        }
        
        // 计算进化点数
        int points = calculateEvolutionPoints(target);
        
        // 如果玩家对生物造成了足够伤害，给予进化点数
        if (shouldGrantPoints(player, target)) {
            ISkillHandler handler = SkillAttachment.getSkillHandler(player);
            handler.addEvolutionPoints(points);
            
            // 显示提示
            player.sendSystemMessage(Component.translatable(
                    "message.corpseorigin.evolution_points_gained", points, target.getName().getString()));
            
            CorpseOrigin.LOGGER.info("玩家 {} 吞噬 {} 获得 {} 进化点数", 
                    player.getName().getString(), target.getName().getString(), points);
        } else {
            CorpseOrigin.LOGGER.debug("玩家 {} 对 {} 造成的伤害不足，无法获得进化点", 
                    player.getName().getString(), target.getName().getString());
        }
        
        // 清理伤害追踪
        cleanupDamageTracking(target);
    }
    
    /**
     * 追踪玩家对生物造成的伤害
     */
    private static void trackDamage(Player player, LivingEntity target, float damage) {
        UUID playerId = player.getUUID();
        UUID targetId = target.getUUID();
        
        damageTracker.computeIfAbsent(playerId, k -> new HashMap<>());
        Map<UUID, Float> playerDamage = damageTracker.get(playerId);
        
        playerDamage.merge(targetId, damage, Float::sum);
    }
    
    /**
     * 判断是否应该给予进化点数（玩家需要造成至少25%的伤害，或者是最后一击）
     */
    private static boolean shouldGrantPoints(Player player, LivingEntity target) {
        UUID playerId = player.getUUID();
        UUID targetId = target.getUUID();

        // 只要玩家参与了攻击就给予进化点（造成伤害即可）
        Map<UUID, Float> playerDamage = damageTracker.get(playerId);
        if (playerDamage == null) {
            // 如果没有追踪到伤害，但玩家是攻击者，仍然给予基础点数
            return true;
        }

        Float damageDealt = playerDamage.get(targetId);
        if (damageDealt == null || damageDealt <= 0) {
            // 玩家是最后一击，但没有追踪到伤害数据，仍然给予点数
            return true;
        }

        // 计算玩家造成的伤害占比
        float maxHealth = target.getMaxHealth();
        float damagePercent = damageDealt / maxHealth;

        // 玩家需要造成至少25%的伤害，或者是最后一击
        return damagePercent >= 0.25f;
    }
    
    /**
     * 清理伤害追踪数据
     */
    private static void cleanupDamageTracking(LivingEntity target) {
        UUID targetId = target.getUUID();
        
        // 从所有玩家的追踪中移除该生物
        for (Map<UUID, Float> playerDamage : damageTracker.values()) {
            playerDamage.remove(targetId);
        }
    }
    
    /**
     * 计算生物提供的进化点数
     */
    private static int calculateEvolutionPoints(LivingEntity entity) {
        MobCategory category = entity.getType().getCategory();
        
        // 特殊生物处理
        if (entity instanceof Player) {
            return 5; // 玩家：5点
        }
        
        // 根据生物类别获取基础点数
        int basePoints = EVOLUTION_POINTS_MAP.getOrDefault(category, 1);
        
        // 根据生物生命值调整
        float maxHealth = entity.getMaxHealth();
        if (maxHealth > 50) {
            basePoints += 2; // 高生命值生物额外奖励
        } else if (maxHealth > 20) {
            basePoints += 1;
        }
        
        // Boss生物额外奖励 (检查是否是Boss)
        if (entity.getMaxHealth() > 100 || entity.hasCustomName()) {
            basePoints += 3;
        }
        
        return basePoints;
    }
    
    /**
     * 判断目标是否是活物（可被吞噬）
     */
    private static boolean isLivingCreature(LivingEntity entity) {
        // 排除已死亡的生物
        if (!entity.isAlive()) {
            return false;
        }
        
        MobCategory category = entity.getType().getCategory();
        
        // 可被吞噬的生物类别
        return category == MobCategory.CREATURE ||
               category == MobCategory.AMBIENT ||
               category == MobCategory.MONSTER ||
               category == MobCategory.AXOLOTLS ||
               category == MobCategory.UNDERGROUND_WATER_CREATURE ||
               category == MobCategory.WATER_CREATURE ||
               category == MobCategory.WATER_AMBIENT ||
               entity instanceof net.minecraft.world.entity.npc.AbstractVillager ||
               entity instanceof Player;
    }
}
