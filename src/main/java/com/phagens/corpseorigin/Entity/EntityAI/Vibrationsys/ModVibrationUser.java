package com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.jetbrains.annotations.Nullable;

public class ModVibrationUser implements VibrationSystem.User {
    private final net.minecraft.world.entity.LivingEntity entity;


    private static final ResourceLocation SPEED_BOOST_UUID =
            ResourceLocation.parse("copymod:a1b2c3d4-e5f6-7890-abcd-ef1234567890");


    public ModVibrationUser(net.minecraft.world.entity.LivingEntity entity) {
        this.entity = entity;
    }

    @Override
    public int getListenerRadius() {
        return 16;//监听半径
    }

    @Override
    public PositionSource getPositionSource() {
        //使用实体的眼睛作为感知源头
        return new EntityPositionSource(entity, entity.getEyeHeight());
    }

    //振动接受条件
    @Override
    public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, GameEvent.Context context) {
        //状态检查
        if (entity instanceof net.minecraft.world.entity.Mob mob && mob.isNoAi()) return false;        // AI是否被禁用
        if (entity.isDeadOrDying()) return false; // 是否濒死
        if (entity.isRemoved()) return false;     // 是否被移除
        // 冷却系统变量
        if (entity instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob && pathfinderMob.getBrain().hasMemoryValue(MemoryModuleType.VIBRATION_COOLDOWN)) {
            return false; // 正在冷却中，拒绝新振动
        }
        // 边界检查变量
        if (!serverLevel.getWorldBorder().isWithinBounds(blockPos)) {
            return false; // 事件发生在世界边界外
        }
        // 目标验证变量
        if (context.sourceEntity() instanceof LivingEntity livingEntity && entity instanceof net.minecraft.world.entity.Mob mob) {
            return mob.canAttack(livingEntity); // 使用实体的攻击判断逻辑
        }
        return true;
    }

    //振动事件处理中心
    @Override
    public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity0, @Nullable Entity entity1, float v) {
        if (!entity.isDeadOrDying()) {
            // 冷却系统 - 只对PathfinderMob类型的实体设置
            if (entity instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
                pathfinderMob.getBrain().setMemoryWithExpiry(MemoryModuleType.VIBRATION_COOLDOWN, Unit.INSTANCE, 40L);//冷却
            }
            // 2. 播放反馈效果
            serverLevel.broadcastEntityEvent(entity, (byte) 61);
            entity.playSound(SoundEvents.WARDEN_TENDRIL_CLICKS, 1.0F, 1.0F);

            // 3. 核心功能：根据振动强度应用速度提升
            VibratiomIntensity intensity = calculateVibrationIntensity(holder,v);
            applySpeedBoost(intensity);
            executeChaseStrategy(intensity, blockPos, entity0);

        }
    }

    //内部枚举
    public enum VibratiomIntensity {
        WEAK(1.2F, 30),      // 微弱：1.2倍速，30刻持续
        MEDIUM(1.8F, 60),    // 中等：1.8倍速，60刻持续
        STRONG(2.5F, 120);   // 强烈：2.5倍速，120刻持续
        private final float speedMultiplier;
        private final int duration;

        VibratiomIntensity(float speedMultiplier, int duration) {
            this.speedMultiplier = speedMultiplier;
            this.duration = duration;
        }

        public float getSpeedMultiplier() {
            return speedMultiplier;
        }

        public int getDuration() {
            return duration;
        }


    }
    private VibratiomIntensity calculateVibrationIntensity(Holder<GameEvent> gameEvent, float distance) {
        // 基础强度值（根据事件类型）
        int baseIntensity = 0;

        if (gameEvent.is(GameEvent.EXPLODE)) {
            baseIntensity = 100;  // 爆炸最强烈
        } else if (gameEvent.is(GameEvent.ENTITY_DAMAGE)) {
            baseIntensity = 70;   // 伤害事件
        } else if (gameEvent.is(GameEvent.PROJECTILE_SHOOT)) {
            baseIntensity = 60;   // 弹射物
        } else if (gameEvent.is(GameEvent.STEP)) {
            baseIntensity = 30;   // 脚步声
        } else if (gameEvent.is(GameEvent.SWIM)) {
            baseIntensity = 25;   // 游泳声
        } else {
            baseIntensity = 20;   // 其他事件
        }
        // 距离衰减（越近越强烈）
        float distanceFactor = Math.max(0.1F, 1.0F - (distance / 32.0F));
        int finalIntensity = (int) (baseIntensity * distanceFactor);

        // 根据最终强度确定等级
        if (finalIntensity >= 80) return VibratiomIntensity.STRONG;
        if (finalIntensity >= 40) return VibratiomIntensity.MEDIUM;
        return VibratiomIntensity.WEAK;
    }

    // 应用速度提升
    private void applySpeedBoost(VibratiomIntensity intensity) {
        // 设置临时速度提升属性
        AttributeInstance speedAttribute = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            // 移除旧的修饰符（如果存在）
            AttributeModifier oldModifier = speedAttribute.getModifier(SPEED_BOOST_UUID);
            if (oldModifier != null) {
                speedAttribute.removeModifier(oldModifier);
            }


            // 添加新的速度提升修饰符
            AttributeModifier speedBoost = new AttributeModifier(
                    SPEED_BOOST_UUID,
                    intensity.getSpeedMultiplier() - 1.0F,  // 转换为加成值
                    AttributeModifier.Operation.ADD_VALUE
            );
            speedAttribute.addTransientModifier(speedBoost);

            // 只对PathfinderMob类型的实体设置记忆
            if (entity instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
                pathfinderMob.getBrain().setMemoryWithExpiry(
                        MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
                        intensity.getDuration(),
                        intensity.getDuration()
                );
            }
        }
    }

    // 执行追击策略
    private void executeChaseStrategy(VibratiomIntensity intensity, BlockPos sourcePos, @Nullable Entity sourceEntity) {
        // 只对PathfinderMob类型的实体执行追击策略
        if (entity instanceof net.minecraft.world.entity.PathfinderMob pathfinderMob) {
            switch (intensity) {
                case WEAK:
                    // 微弱振动：好奇探索
                    pathfinderMob.getBrain().setMemoryWithExpiry(
                            MemoryModuleType.DISTURBANCE_LOCATION,
                            sourcePos,
                            100L
                    );
                    pathfinderMob.getBrain().setActiveActivityIfPossible(Activity.INVESTIGATE);
                    break;

                case MEDIUM:
                    // 中等振动：主动搜索
                    if (sourceEntity instanceof LivingEntity livingEntity) {
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.LOOK_TARGET,
                                new EntityTracker(livingEntity, true)
                        );
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.WALK_TARGET,
                                new WalkTarget(new EntityTracker(livingEntity, false), 1.5F, 0)
                        );
                    } else {
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.WALK_TARGET,
                                new WalkTarget(sourcePos, 1.5F, 5)
                        );
                    }
                    pathfinderMob.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
                    break;

                case STRONG:
                    // 强烈振动：全力追击
                    if (sourceEntity instanceof LivingEntity livingEntity &&
                            pathfinderMob.canAttack(livingEntity)) {
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.ATTACK_TARGET,
                                livingEntity
                        );
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.WALK_TARGET,
                                new WalkTarget(new EntityTracker(livingEntity, false), 2.0F, 0)
                        );
                    } else {
                        pathfinderMob.getBrain().setMemory(
                                MemoryModuleType.WALK_TARGET,
                                new WalkTarget(sourcePos, 2.0F, 3)
                        );
                    }
                    pathfinderMob.getBrain().setActiveActivityIfPossible(Activity.FIGHT);
                    break;
            }
        }

    }
}
