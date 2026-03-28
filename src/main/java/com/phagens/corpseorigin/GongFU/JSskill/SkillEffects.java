package com.phagens.corpseorigin.GongFU.JSskill;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import static net.minecraft.commands.arguments.ParticleArgument.getParticle;

public class SkillEffects {
    /**
     * 在玩家周围生成粒子效果
     *
     * @param player 施法玩家
     * @param particleId 粒子 ID（如 "minecraft:cloud"）
     * @param count 粒子数量
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param offsetX X 扩散范围
     * @param offsetY Y 扩散范围
     * @param offsetZ Z 扩散范围
     * @param speed 粒子速度
     */
    public static void spawnParticles(ServerPlayer player, String particleId, int count,
                                      double x, double y, double z,
                                      double offsetX, double offsetY, double offsetZ, double speed) {
        try {
            // 根据 ID 获取粒子类型
            ParticleOptions particle = getParticle(particleId);
            if (particle == null) return;

            // 创建粒子数据包
            var packet = new net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket(
                    particle, true,
                    (float) x, (float) y, (float) z,
                    (float) offsetX, (float) offsetY, (float) offsetZ,
                    (float) speed, count
            );

            player.connection.send(packet);
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("生成粒子失败：{}", particleId, e);
        }
    }

    /**
     * 播放音效
     *
     * @param player 施法玩家
     * @param soundId 音效 ID（如 "minecraft:block.anvil.place"）
     * @param volume 音量 (0.0-1.0)
     * @param pitch 音调 (0.5-2.0)
     */
    public static void playSound(ServerPlayer player, String soundId, float volume, float pitch) {
        try {
            // 解析音效 ID
            ResourceLocation rl = ResourceLocation.parse(soundId);
            var soundEvent = BuiltInRegistries.SOUND_EVENT.get(rl);

            if (soundEvent != null) {  // ✅ 直接判断是否为 null
                player.serverLevel().playSound(
                        player,
                        player.getX(), player.getY(), player.getZ(),
                        soundEvent,   // ✅ 直接使用 soundEvent
                        SoundSource.PLAYERS,
                        volume, pitch
                );
            } else {
                CorpseOrigin.LOGGER.warn("未找到音效：{}", soundId);
            }
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("播放音效失败：{}", soundId, e);
        }
    }

    /**
     * 给玩家添加状态效果
     *
     * @param player 目标玩家
     * @param effectId 效果 ID（如 "minecraft:resistance"）
     * @param duration 持续时间（tick，20tick=1 秒）
     * @param amplifier 效果等级（0=I 级，1=II 级）
     */
    public static void addEffect(ServerPlayer player, String effectId, int duration, int amplifier) {
        try {
            // 解析效果 ID
            ResourceLocation rl = ResourceLocation.parse(effectId);
            var effect = BuiltInRegistries.MOB_EFFECT.get(rl);

            if (effect != null) {
                var effectHolder = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect);
                player.addEffect(new MobEffectInstance(
                        effectHolder,
                        duration,
                        amplifier,
                        false,
                        false
                ));
            } else {
                CorpseOrigin.LOGGER.warn("未找到效果：{}", effectId);
            }
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("添加效果失败：{}", effectId, e);
        }
    }

    /**
     * 对生物造成伤害
     *
     * @param attacker 攻击者
     * @param target 目标生物
     * @param amount 伤害值
     */
    public static void damageTarget(ServerPlayer attacker, LivingEntity target, float amount) {
        try {
            // 创建玩家攻击伤害来源
            DamageSource damageSource = createPlayerDamageSource(attacker);
            if (damageSource != null) {
                target.hurt(damageSource, amount);
            }
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("造成伤害失败", e);
        }
    }

    /**
     * 对目标造成魔法伤害
     *
     * @param attacker 攻击者
     * @param target 目标生物
     * @param amount 伤害值
     */
    public static void magicDamage(ServerPlayer attacker, LivingEntity target, float amount) {
        try {
            DamageSource damageSource = createMagicDamageSource(attacker);
            if (damageSource != null) {
                target.hurt(damageSource, amount);
            }
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("魔法伤害失败", e);
        }
    }

    /**
     * 击退目标
     *
     * @param attacker 攻击者
     * @param target 目标生物
     * @param strength 击退强度 (0.0-2.0)
     */
    public static void knockback(ServerPlayer attacker, LivingEntity target, double strength) {
        try {
            double dx = target.getX() - attacker.getX();
            double dz = target.getZ() - attacker.getZ();

            // 归一化方向向量
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dz /= distance;
            }

            // 应用击退
            target.push(dx * strength, 0.5, dz * strength);
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("击退失败", e);
        }
    }

    /**
     * 治疗目标
     *
     * @param target 目标生物
     * @param amount 治疗量
     */
    public static void heal(LivingEntity target, float amount) {
        try {
            target.heal(amount);
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("治疗失败", e);
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取粒子类型
     */
    private static ParticleOptions getParticle(String id) {
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            // ✅ 直接从注册表获取已注册的粒子类型
            var particleType = BuiltInRegistries.PARTICLE_TYPE.get(rl);

            if (particleType != null) {
                return getParticleOptionsFromType(particleType);
            } else {
                CorpseOrigin.LOGGER.warn("粒子类型不存在：{}，使用默认", id);
                return ParticleTypes.CLOUD;
            }
        } catch (Exception e) {
            CorpseOrigin.LOGGER.warn("粒子类型不存在：{}，使用默认", id);
            return ParticleTypes.CLOUD;
        }
    }

    /**
     * 从 ParticleType 获取 ParticleOptions
     */
    private static ParticleOptions getParticleOptionsFromType(net.minecraft.core.particles.ParticleType<?> particleType) {
        // ✅ 其他类型尝试直接转换
        try {
            return (ParticleOptions) particleType;
        } catch (ClassCastException e) {
            return ParticleTypes.CLOUD;
        }
    }

    /**
     * 创建玩家攻击伤害来源
     */
    private static DamageSource createPlayerDamageSource(ServerPlayer player) {
        try {
            // ✅ 使用 DamageSources 获取标准的伤害类型
            return player.damageSources().playerAttack(player);
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("创建玩家伤害来源失败", e);
        }
        return null;
    }

    /**
     * 创建魔法伤害来源
     */
    private static DamageSource createMagicDamageSource(ServerPlayer player) {
        try {
            // ✅ 使用 DamageSources 获取标准的魔法伤害类型
            return player.damageSources().magic();
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("创建魔法伤害来源失败", e);
        }
        return null;
    }
}
