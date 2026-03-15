package com.phagens.corpseorigin.Entity;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;

public class LongyouEntity extends PathfinderMob implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation SHIEYE_ANIM = RawAnimation.begin().thenPlay("shieye");

    private static final EntityDataAccessor<Boolean> DATA_PLAYING_SHIEYE =
            SynchedEntityData.defineId(LongyouEntity.class, EntityDataSerializers.BOOLEAN);

    private int shieyeCooldown = 0;
    private int shieyeAnimationTicks = 0;

    // 龙右的智能系统
    private int intelligenceCheckCooldown = 0;
    private static final int INTELLIGENCE_CHECK_INTERVAL = 100; // 每5秒检查一次

    // 手下列表（被龙右认可的尸兄）
    private final Set<UUID> minions = new HashSet<>();
    // 粮仓列表（被标记为食物的尸兄）
    private final Set<UUID> foodReserves = new HashSet<>();
    // 手下数量上限
    private static final int MAX_MINIONS = 10;
    // 识别范围
    private static final double RECOGNITION_RANGE = 64.0D;

    public LongyouEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYING_SHIEYE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // 龙右作为尸王，不会攻击尸兄（同类），但会攻击其他生物
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Animal.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        // 不会主动攻击 LowerLevelZbEntity（尸兄同类）
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 500.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 15.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::controlAnimation));
    }

    private <E extends LongyouEntity> software.bernie.geckolib.animation.PlayState controlAnimation(AnimationState<E> event) {
        if (this.entityData.get(DATA_PLAYING_SHIEYE)) {
            return event.setAndContinue(SHIEYE_ANIM);
        }

        if (this.getAttackAnim(event.getPartialTick()) > 0) {
            if (!this.level().isClientSide && shieyeCooldown <= 0 && this.random.nextFloat() < 0.4F) {
                triggerShieyeAnimation();
                return event.setAndContinue(SHIEYE_ANIM);
            }
            return event.setAndContinue(ATTACK_ANIM);
        }

        if (event.isMoving()) {
            return event.setAndContinue(WALK_ANIM);
        }

        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void tick() {
        super.tick();

        if (shieyeCooldown > 0) {
            shieyeCooldown--;
        }

        if (!this.level().isClientSide && this.entityData.get(DATA_PLAYING_SHIEYE)) {
            shieyeAnimationTicks--;
            if (shieyeAnimationTicks <= 0) {
                this.entityData.set(DATA_PLAYING_SHIEYE, false);
            }
        }

        // 服务端：执行智能判断
        if (!this.level().isClientSide && this.level() instanceof ServerLevel) {
            if (intelligenceCheckCooldown-- <= 0) {
                intelligenceCheckCooldown = INTELLIGENCE_CHECK_INTERVAL;
                performIntelligenceCheck();
            }
        }
    }

    /**
     * 龙右的智能判断系统
     * 评估周围的尸兄，决定哪些是手下，哪些是粮仓
     */
    private void performIntelligenceCheck() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        // 获取范围内的所有尸兄
        List<LowerLevelZbEntity> nearbyZombies = serverLevel.getEntitiesOfClass(
                LowerLevelZbEntity.class,
                this.getBoundingBox().inflate(RECOGNITION_RANGE)
        );

        for (LowerLevelZbEntity zb : nearbyZombies) {
            UUID zbId = zb.getUUID();

            // 如果已经分类过，跳过
            if (minions.contains(zbId) || foodReserves.contains(zbId)) {
                continue;
            }

            // 评估这个尸兄的价值
            ZombieValue value = evaluateZombie(zb);

            if (value == ZombieValue.MINION && minions.size() < MAX_MINIONS) {
                // 收为手下
                minions.add(zbId);
                CorpseOrigin.LOGGER.info("龙右将尸兄 {} 收为手下", zbId);
                // 可以在这里添加视觉效果或状态效果
                serverLevel.broadcastEntityEvent(zb, (byte) 7); // 爱心粒子效果
            } else {
                // 标记为粮仓
                foodReserves.add(zbId);
                CorpseOrigin.LOGGER.info("龙右将尸兄 {} 标记为粮仓", zbId);
            }
        }

        // 清理已死亡的尸兄
        minions.removeIf(id -> serverLevel.getEntity(id) == null || !(serverLevel.getEntity(id) instanceof LowerLevelZbEntity));
        foodReserves.removeIf(id -> serverLevel.getEntity(id) == null || !(serverLevel.getEntity(id) instanceof LowerLevelZbEntity));
    }

    /**
     * 评估尸兄的价值
     */
    private ZombieValue evaluateZombie(LowerLevelZbEntity zb) {
        int level = zb.getEvolutionLevel();
        int kills = zb.getKills();
        float healthPercent = zb.getHealth() / zb.getMaxHealth();

        // 计算潜力值
        int potential = level * 10 + kills + (int)(healthPercent * 10);

        // 潜力值高的适合做手下
        if (potential >= 25) {
            return ZombieValue.MINION;
        }

        // 潜力值低的作为粮仓
        return ZombieValue.FOOD_RESERVE;
    }

    /**
     * 判断是否应该攻击某个目标
     * 龙右作为尸王，不会食用同类（尸兄）
     */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        // 龙右不会攻击尸兄（同类）
        if (entity instanceof LowerLevelZbEntity) {
            CorpseOrigin.LOGGER.debug("龙右拒绝攻击尸兄（同类）");
            return false;
        }

        return super.doHurtTarget(entity);
    }

    /**
     * 龙右是否需要进食
     * 作为尸王，他不需要像普通尸兄那样吞噬同类
     */
    public boolean needsToEat() {
        // 龙右作为尸王，生命值低于50%时才会考虑进食
        return this.getHealth() < this.getMaxHealth() * 0.5;
    }

    /**
     * 消耗粮仓（当需要恢复时）
     */
    public void consumeFoodReserve() {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        if (foodReserves.isEmpty()) return;

        // 获取一个粮仓
        UUID foodId = foodReserves.iterator().next();
        if (serverLevel.getEntity(foodId) instanceof LowerLevelZbEntity food) {
            CorpseOrigin.LOGGER.info("龙右消耗粮仓 {} 恢复生命值", foodId);

            // 恢复生命值
            this.heal(this.getMaxHealth() * 0.3F);

            // 播放效果
            serverLevel.broadcastEntityEvent(this, (byte) 35);
            this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 2.0F, 0.8F);

            // 移除粮仓
            food.discard();
            foodReserves.remove(foodId);
        }
    }

    /**
     * 获取手下数量
     */
    public int getMinionCount() {
        return minions.size();
    }

    /**
     * 获取粮仓数量
     */
    public int getFoodReserveCount() {
        return foodReserves.size();
    }

    private void triggerShieyeAnimation() {
        this.entityData.set(DATA_PLAYING_SHIEYE, true);
        this.shieyeCooldown = 150;
        this.shieyeAnimationTicks = 60;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("PlayingShieye", this.entityData.get(DATA_PLAYING_SHIEYE));
        compound.putInt("ShieyeCooldown", this.shieyeCooldown);

        // 保存手下列表
        CompoundTag minionsTag = new CompoundTag();
        int i = 0;
        for (UUID id : minions) {
            minionsTag.putUUID("Minion" + i++, id);
        }
        compound.put("Minions", minionsTag);
        compound.putInt("MinionCount", minions.size());

        // 保存粮仓列表
        CompoundTag foodTag = new CompoundTag();
        i = 0;
        for (UUID id : foodReserves) {
            foodTag.putUUID("Food" + i++, id);
        }
        compound.put("FoodReserves", foodTag);
        compound.putInt("FoodReserveCount", foodReserves.size());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("PlayingShieye")) {
            this.entityData.set(DATA_PLAYING_SHIEYE, compound.getBoolean("PlayingShieye"));
        }
        if (compound.contains("ShieyeCooldown")) {
            this.shieyeCooldown = compound.getInt("ShieyeCooldown");
        }

        // 读取手下列表
        if (compound.contains("Minions")) {
            CompoundTag minionsTag = compound.getCompound("Minions");
            int count = compound.getInt("MinionCount");
            for (int i = 0; i < count; i++) {
                if (minionsTag.hasUUID("Minion" + i)) {
                    minions.add(minionsTag.getUUID("Minion" + i));
                }
            }
        }

        // 读取粮仓列表
        if (compound.contains("FoodReserves")) {
            CompoundTag foodTag = compound.getCompound("FoodReserves");
            int count = compound.getInt("FoodReserveCount");
            for (int i = 0; i < count; i++) {
                if (foodTag.hasUUID("Food" + i)) {
                    foodReserves.add(foodTag.getUUID("Food" + i));
                }
            }
        }
    }

    /**
     * 尸兄价值枚举
     */
    private enum ZombieValue {
        MINION,      // 手下 - 有潜力的尸兄
        FOOD_RESERVE // 粮仓 - 作为储备食物的尸兄
    }
}
