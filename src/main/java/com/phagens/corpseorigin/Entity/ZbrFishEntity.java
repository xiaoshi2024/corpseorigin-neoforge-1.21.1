package com.phagens.corpseorigin.Entity;

import com.phagens.corpseorigin.Entity.EntityAI.JLAI.ModFollow;
import com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys.ModVibrationUser;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

public class ZbrFishEntity extends AbstractFish implements GeoEntity, VibrationSystem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation SWIM_ANIM = RawAnimation.begin().thenLoop("swim");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    
    // 振动系统必需的字段
    private final DynamicGameEventListener<Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    private VibrationSystem.Data vibrationData;
    
    // 尸族特性字段
    private int evolutionLevel = 1; // 进化等级
    private int hunger = 100; // 饥饿度 (0-100)
    private static final int HUNGER_THRESHOLD = 20; // 饥饿阈值
    private int lastHurtTick = -1000; // 上次被攻击的游戏刻
    private static final int HURT_MEMORY_DURATION = 200; // 被攻击记忆持续时间（10秒）

    public ZbrFishEntity(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
        this.vibrationUser = new ModVibrationUser(this);
        this.vibrationData = new VibrationSystem.Data();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));
    }
    
    // 自定义鱼类移动控制
    private static class FishMoveControl extends MoveControl {
        private final ZbrFishEntity fish;
        
        public FishMoveControl(ZbrFishEntity fish) {
            super(fish);
            this.fish = fish;
        }
        
        @Override
        public void tick() {
            if (this.fish.isInWater()) {
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, 0.005D, 0.0D));
            }
            
            if (this.operation == Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
                double dx = this.wantedX - this.fish.getX();
                double dy = this.wantedY - this.fish.getY();
                double dz = this.wantedZ - this.fish.getZ();
                
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                dy = dy / distance;
                float speed = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                
                this.fish.setSpeed(speed);
                this.fish.setXRot(this.rotlerp(this.fish.getXRot(), (float)(-(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * (180D / Math.PI))), 10.0F));
                this.fish.yHeadRot = this.fish.getXRot();
                
                double movementFactor = this.fish.isInWater() ? 0.1D : 0.02D;
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(dx * movementFactor, dy * movementFactor, dz * movementFactor));
            } else {
                this.fish.setSpeed(0.0F);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new ModFollow(this, 1.0D, true));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }
    
    protected void addBehaviourGoals() {
        // 攻击目标 - 使用自定义条件判断是否攻击
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, this::shouldAttackTarget));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, 0, true, false, this::shouldAttackTarget));
    }
    
    /**
     * 判断是否应该攻击目标
     * 尸兄鱼不会主动攻击尸兄玩家（同类），除非极度饥饿或被攻击
     */
    private boolean shouldAttackTarget(net.minecraft.world.entity.LivingEntity entity) {
        // 不攻击龙右（尸王）
        if (entity instanceof LongyouEntity) {
            return false;
        }
        
        // 检查目标是否已成为尸兄的玩家（同类）
        if (entity instanceof Player player) {
            if (com.phagens.corpseorigin.player.PlayerCorpseData.isCorpse(player)) {
                // 如果被攻击了，允许反击
                boolean wasRecentlyHurt = (this.tickCount - lastHurtTick) < HURT_MEMORY_DURATION;
                if (wasRecentlyHurt) {
                    return true; // 被攻击时反击同类玩家
                }
                
                // 同类尸兄玩家，只有在极度饥饿时才攻击
                boolean isHungry = this.hunger <= HUNGER_THRESHOLD;
                
                // 只有在极度饥饿时才攻击同类玩家
                if (!isHungry) {
                    return false; // 不饥饿时不攻击同类
                }
            }
        }
        
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.5D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "swim_controller", 5, this::swimAnimation));
    }

    private <E extends ZbrFishEntity> software.bernie.geckolib.animation.PlayState swimAnimation(AnimationState<E> event) {
        if (event.isMoving()) {
            return event.setAndContinue(SWIM_ANIM);
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
        if (!this.level().isClientSide) {
            VibrationSystem.Ticker.tick((net.minecraft.server.level.ServerLevel) this.level(), this.vibrationData, this.vibrationUser);
            
            // 尸族特性：每5秒减少1点饥饿度
            if (this.tickCount % 100 == 0 && hunger > 0) {
                hunger--;
            }
            
            // 尸族特性：每10秒被动恢复生命
            if (this.tickCount % 200 == 0) {
                if (this.getHealth() < this.getMaxHealth()) {
                    float healAmount = 0.5f + (evolutionLevel * 0.3f); // 基础恢复 + 等级加成
                    this.heal(healAmount);
                }
            }
        }
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        // 记录被攻击的时间（用于反击逻辑）
        if (source.getEntity() instanceof net.minecraft.world.entity.LivingEntity) {
            lastHurtTick = this.tickCount;
        }
        return super.hurt(source, amount);
    }
    
    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        // 尸族免疫普通毒素
        if (effect.getEffect().value() == net.minecraft.world.effect.MobEffects.POISON.value() ||
            effect.getEffect().value() == net.minecraft.world.effect.MobEffects.HUNGER.value() ||
            effect.getEffect().value().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
            return false;
        }
        return super.canBeAffected(effect);
    }
    
    @Override
    public Data getVibrationData() {
        return this.vibrationData;
    }
    
    @Override
    public User getVibrationUser() {
        return this.vibrationUser;
    }
    
    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence();
    }
    
    @Override
    public int getMaxAirSupply() {
        return 300;
    }
    
    @Override
    public void baseTick() {
        super.baseTick();
        if (!this.isInWater() && this.onGround() && this.horizontalCollision) {
            this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() * 2.0F - 1.0F) * 0.05F, 0.4F, (this.random.nextFloat() * 2.0F - 1.0F) * 0.05F));
            this.hasImpulse = true;
            this.playSound(this.getFlopSound(), this.getSoundVolume(), 1.0F);
        }
    }
    
    protected net.minecraft.sounds.SoundEvent getFlopSound() {
        return net.minecraft.sounds.SoundEvents.COD_FLOP;
    }
    
    @Override
    public boolean isPushedByFluid() {
        return false;
    }
    
    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }
    
    // 使尸兄鱼能够在水中呼吸
    @Override
    public int getAirSupply() {
        return 300;
    }
    
    @Override
    public net.minecraft.world.item.ItemStack getBucketItemStack() {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("EvolutionLevel", this.evolutionLevel);
        compound.putInt("Hunger", this.hunger);
        compound.putInt("LastHurtTick", this.lastHurtTick);
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("EvolutionLevel")) {
            this.evolutionLevel = compound.getInt("EvolutionLevel");
        }
        if (compound.contains("Hunger")) {
            this.hunger = compound.getInt("Hunger");
        }
        if (compound.contains("LastHurtTick")) {
            this.lastHurtTick = compound.getInt("LastHurtTick");
        }
    }
    
    /**
     * 攻击目标时，有一定概率感染村民
     * 尸族特性：攻击活物时恢复生命值和饥饿度
     */
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        // 检查是否应该攻击尸兄玩家
        if (entity instanceof Player player) {
            if (com.phagens.corpseorigin.player.PlayerCorpseData.isCorpse(player)) {
                // 如果被攻击了，允许反击
                boolean wasRecentlyHurt = (this.tickCount - lastHurtTick) < HURT_MEMORY_DURATION;
                if (!wasRecentlyHurt) {
                    // 同类尸兄玩家，只有在极度饥饿时才攻击
                    boolean isHungry = this.hunger <= HUNGER_THRESHOLD;
                    if (!isHungry) {
                        return false; // 不饥饿时不攻击同类
                    }
                }
            }
        }
        
        boolean result = super.doHurtTarget(entity);
        
        if (result && !this.level().isClientSide) {
            // 尸族特性：攻击活物时恢复生命值和饥饿度
            if (entity instanceof net.minecraft.world.entity.LivingEntity target) {
                // 恢复饥饿度
                hunger = Math.min(100, hunger + 5);
                
                // 恢复生命值
                float healAmount = 1.0f + (evolutionLevel * 0.5f);
                this.heal(healAmount);
            }
            
            // 随机感染村民
            if (entity instanceof net.minecraft.world.entity.npc.Villager villager) {
                if (this.random.nextFloat() < 0.3) { // 30% 概率感染
                    infectVillager(villager);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 感染村民为尸兄
     */
    private void infectVillager(net.minecraft.world.entity.npc.Villager villager) {
        if (!(this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        
        // 使用 BYeffect 来感染村民（3-15秒随机延迟后变异）
        com.phagens.corpseorigin.Effect.BYeffect.applyInfection(villager, serverLevel);
    }
}