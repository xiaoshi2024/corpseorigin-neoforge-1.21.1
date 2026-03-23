/**
 * 黄色强化剂副作用效果类
 *
 * 【功能说明】
 * 1. 副作用积累：每次使用黄色强化剂会增加副作用等级
 * 2. 负面效果：根据副作用等级给予玩家负面效果
 * 3. 暴毙机制：副作用等级达到阈值时，玩家会暴毙
 * 4. 中和机制：使用蓝色中和剂可以清除副作用
 *
 * 【副作用等级效果】
 * - 等级1-2：轻微恶心，偶尔呕吐（恶心效果）
 * - 等级3-4：身体虚弱，行动迟缓（缓慢+虚弱效果）
 * - 等级5+：身体崩溃，可能暴毙（持续伤害+暴毙风险）
 *
 * 【剧情设定】
 * 黄色强化剂是初期不稳定的实验药剂，直接使用会对身体造成负担。
 * 需要使用蓝色中和剂来稳定药效，否则身体会承受不住而崩溃。
 *
 * @author Phagens
 * @version 1.0
 */
package com.phagens.corpseorigin.effect;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.register.EffectRegister;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class SideEffect extends MobEffect {

    // 副作用等级阈值，超过此值可能导致暴毙
    public static final int DEATH_THRESHOLD = 5;
    // 暴毙概率（每tick检查一次）
    public static final double DEATH_CHANCE = 0.02;
    // 副作用持续时间（tick）- 10分钟
    public static final int EFFECT_DURATION = 12000;

    public SideEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每20tick（1秒）应用一次效果
        return duration % 20 == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().isClientSide) {
            return true;
        }

        // 根据副作用等级给予不同的负面效果
        applySideEffects(livingEntity, amplifier);

        // 检查暴毙条件
        checkDeathRisk(livingEntity, amplifier);

        return true;
    }

    /**
     * 根据副作用等级应用不同的负面效果
     */
    private void applySideEffects(LivingEntity entity, int level) {
        // 等级1-2：恶心效果
        if (level >= 1) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.CONFUSION,
                    100, // 5秒
                    0,
                    false,
                    true,
                    true
            ));
        }

        // 等级3-4：缓慢和虚弱
        if (level >= 3) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN,
                    100,
                    Math.min(level - 3, 1),
                    false,
                    true,
                    true
            ));
            entity.addEffect(new MobEffectInstance(
                    MobEffects.WEAKNESS,
                    100,
                    Math.min(level - 3, 1),
                    false,
                    true,
                    true
            ));
        }

        // 等级5+：持续伤害
        if (level >= 5) {
            entity.addEffect(new MobEffectInstance(
                    MobEffects.POISON,
                    100,
                    Math.min(level - 5, 2),
                    false,
                    true,
                    true
            ));
        }
    }

    /**
     * 检查暴毙风险
     */
    private void checkDeathRisk(LivingEntity entity, int level) {
        if (level < DEATH_THRESHOLD) {
            return;
        }

        // 等级越高，暴毙概率越大
        double deathChance = DEATH_CHANCE * (level - DEATH_THRESHOLD + 1);

        if (entity.getRandom().nextDouble() < deathChance) {
            // 暴毙！
            if (entity instanceof ServerPlayer player) {
                // 发送死亡消息
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§c§l你的身体无法承受强化剂的副作用，崩溃了..."
                ));

                // 造成致命伤害
                entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);

                CorpseOrigin.LOGGER.info("玩家 {} 因强化剂副作用暴毙", player.getName().getString());
            } else {
                entity.hurt(entity.damageSources().magic(), Float.MAX_VALUE);
            }
        }
    }

    /**
     * 给玩家添加副作用（静态方法）
     * @param player 目标玩家
     * @param level 副作用等级增量
     */
    public static void applySideEffect(Player player, int level) {
        if (player.level().isClientSide) {
            return;
        }

        // 检查是否已有副作用效果
        MobEffectInstance existingEffect = player.getEffect(EffectRegister.SIDE_EFFECT);

        int newLevel = level;
        int newDuration = EFFECT_DURATION;

        if (existingEffect != null) {
            // 叠加等级
            newLevel = existingEffect.getAmplifier() + level;
            // 刷新持续时间
            newDuration = Math.max(existingEffect.getDuration(), EFFECT_DURATION);
        }

        // 限制最大等级为10
        newLevel = Math.min(newLevel, 10);

        // 应用新的副作用效果
        player.addEffect(new MobEffectInstance(
                EffectRegister.SIDE_EFFECT,
                newDuration,
                newLevel,
                false,
                true,
                true
        ));

        // 发送警告消息
        sendWarningMessage(player, newLevel);

        CorpseOrigin.LOGGER.info("玩家 {} 获得副作用效果，当前等级: {}", player.getName().getString(), newLevel);
    }

    /**
     * 根据副作用等级发送警告消息
     */
    private static void sendWarningMessage(Player player, int level) {
        String message;
        if (level == 1) {
            message = "§e你感到有些不适...";
        } else if (level <= 2) {
            message = "§e副作用开始显现，你需要蓝色中和剂来稳定身体...";
        } else if (level <= 4) {
            message = "§c你的身体开始承受不住强化剂的负担！快使用蓝色中和剂！";
        } else {
            message = "§4§l警告：你的身体即将崩溃！立即使用蓝色中和剂！";
        }
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(message));
    }

    /**
     * 清除玩家的副作用（用于蓝色中和剂）
     * @param player 目标玩家
     */
    public static void clearSideEffect(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        MobEffectInstance existingEffect = player.getEffect(EffectRegister.SIDE_EFFECT);

        if (existingEffect != null) {
            // 减少副作用等级
            int currentLevel = existingEffect.getAmplifier();
            int newLevel = Math.max(0, currentLevel - 3); // 蓝色中和剂减少3级副作用

            if (newLevel > 0) {
                // 如果还有剩余副作用，继续应用但等级降低
                player.addEffect(new MobEffectInstance(
                        EffectRegister.SIDE_EFFECT,
                        existingEffect.getDuration(),
                        newLevel,
                        false,
                        true,
                        true
                ));
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a中和剂起效了，副作用减轻了一些..."
                ));
            } else {
                // 完全清除副作用
                player.removeEffect(EffectRegister.SIDE_EFFECT);
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "§a§l副作用已完全清除，你感觉好多了！"
                ));
            }

            CorpseOrigin.LOGGER.info("玩家 {} 使用中和剂，副作用从 {} 级降至 {} 级",
                    player.getName().getString(), currentLevel, newLevel);
        }
    }

    /**
     * 获取玩家当前的副作用等级
     * @param player 目标玩家
     * @return 副作用等级，无效果时返回0
     */
    public static int getSideEffectLevel(Player player) {
        MobEffectInstance effect = player.getEffect(EffectRegister.SIDE_EFFECT);
        return effect != null ? effect.getAmplifier() : 0;
    }
}
