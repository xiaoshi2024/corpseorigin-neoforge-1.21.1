package com.phagens.corpseorigin.Entity;

import com.phagens.corpseorigin.Entity.EntityAI.JLAI.ModFollow;
import com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys.ModVibrationUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
        // 攻击目标
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
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
        }
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
}