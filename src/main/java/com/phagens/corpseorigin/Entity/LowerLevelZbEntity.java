package com.phagens.corpseorigin.Entity;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Entity.EntityAI.Vibrationsys.ModVibrationUser;
import com.phagens.corpseorigin.client.skin.ZbSkinLoader;
import com.phagens.corpseorigin.client.skin.ZbSkinState;
import com.phagens.corpseorigin.network.ZbSkinUpdatePacket;
import com.phagens.corpseorigin.register.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

public class  LowerLevelZbEntity extends PathfinderMob implements GeoEntity, VibrationSystem {
    // 变种类型枚举
    public enum Variant {
        NORMAL(0),
        CRACKED(1); // 裂口尸兄

        private final int code;

        Variant(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static Variant fromCode(int code) {
            for (Variant variant : values()) {
                if (variant.code == code) {
                    return variant;
                }
            }
            return NORMAL;
        }
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
    protected static final RawAnimation SHIEYE_ANIM = RawAnimation.begin().thenPlay("shieye");

    // 同步数据
    private static final EntityDataAccessor<String> DATA_PLAYER_NAME =
            SynchedEntityData.defineId(LowerLevelZbEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DATA_CUSTOM_ID =
            SynchedEntityData.defineId(LowerLevelZbEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Integer> DATA_SKIN_STATE =
            SynchedEntityData.defineId(LowerLevelZbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PLAYING_SHIEYE =
            SynchedEntityData.defineId(LowerLevelZbEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_VARIANT =
            SynchedEntityData.defineId(LowerLevelZbEntity.class, EntityDataSerializers.INT);

    private UUID playerSkinId;
    private String playerSkinName;

    // 皮肤相关（仅客户端使用）- 直接在声明时初始化
    @OnlyIn(Dist.CLIENT)
    private ResourceLocation skinTexture;

    // 皮肤加载状态标记 - 需要在两端都存在
    private boolean skinLoadStarted = false;
    
    // 动画状态跟踪
    private int shieyeCooldown = 0;
    private int shieyeAnimationTicks = 0;
    
    // 进化相关
    private int evolutionLevel = 1;
    private int kills = 0;
    
    // 饥饿度系统 (0-100, 100为饱腹, 0为极度饥饿)
    private int hunger = 100;
    private static final int HUNGER_THRESHOLD_FOR_CANNIBALISM = 20; // 饥饿度低于20才允许吞噬
    
    // 神志系统
    private boolean hasSentient = false; // 是否保留生前神志
    private int speechCooldown = 0; // 说话冷却时间
    
    // 贪婪系统
    private boolean isGreedy = false; // 是否贪婪
    private int greedCooldown = 0; // 贪婪行为冷却时间

    // 振动系统
    private final DynamicGameEventListener<Listener> dynamicGameEventListener;
    private final VibrationSystem.User vibrationUser;
    private VibrationSystem.Data vibrationData;

    public LowerLevelZbEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.vibrationUser = new ModVibrationUser(this);
        this.vibrationData = new VibrationSystem.Data();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationSystem.Listener(this));

        // 初始化神志和贪婪系统
        this.hasSentient = this.random.nextFloat() < 0.3; // 30%概率保留生前神志
        this.isGreedy = this.random.nextFloat() < 0.5; // 50%概率贪婪

        // 客户端字段已经在声明时初始化，不需要在这里处理
    }

    public LowerLevelZbEntity(EntityType<? extends PathfinderMob> entityType, Level level, Player player) {
        this(entityType, level);
        if (player != null) {
            this.playerSkinId = player.getUUID();
            this.playerSkinName = player.getName().getString();
            this.entityData.set(DATA_PLAYER_NAME, this.playerSkinName);
            CorpseOrigin.LOGGER.info("尸兄感染玩家: {}, 将使用其皮肤", this.playerSkinName);
        }
    }

    /**
     * 设置离线玩家的皮肤名称（用于指令召唤）
     */
    public void setPlayerSkinName(String playerName) {
        this.playerSkinName = playerName;
        this.entityData.set(DATA_PLAYER_NAME, playerName);
        CorpseOrigin.LOGGER.info("尸兄设置皮肤玩家: {}", playerName);

        if (!this.level().isClientSide) {
            updateCustomName();
        }
    }

    /**
     * 设置自定义 ID（用于指令召唤）
     */
    // 在设置自定义ID时调用
    public void setCustomId(String customId) {
        this.entityData.set(DATA_CUSTOM_ID, customId);
        CorpseOrigin.LOGGER.info("尸兄设置自定义ID: {}", customId);

        if (!this.level().isClientSide) {
            updateCustomName();
        }
    }

    /**
     * 获取自定义 ID
     */
    public String getCustomId() {
        return this.entityData.get(DATA_CUSTOM_ID);
    }

    /**
     * 获取变种类型
     */
    public Variant getVariant() {
        return Variant.fromCode(this.entityData.get(DATA_VARIANT));
    }

    /**
     * 设置变种类型
     */
    public void setVariant(Variant variant) {
        this.entityData.set(DATA_VARIANT, variant.getCode());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PLAYER_NAME, "");
        builder.define(DATA_CUSTOM_ID, "");
        builder.define(DATA_SKIN_STATE, ZbSkinState.NOT_LOADED.getCode());
        builder.define(DATA_PLAYING_SHIEYE, false);
        builder.define(DATA_VARIANT, Variant.NORMAL.getCode());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        // 近战攻击行为 - 当找到目标时会执行攻击
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        
        // 主动攻击活人
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        
        // 攻击其他生物
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.animal.Animal.class, true));
        
        // 攻击其他怪物（包括其他尸兄），但排除龙右（真王）
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.Mob.class, 0, true, false, this::shouldAttackMob));
    }
    
    /**
     * 判断是否应该攻击某个生物
     * 尸兄不会攻击龙右（真王）
     */
    private boolean shouldAttackMob(net.minecraft.world.entity.LivingEntity entity) {
        // 不攻击龙右（真王）
        if (entity instanceof LongyouEntity) {
            return false;
        }
        return true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    // 添加服务端设置皮肤的方法（由网络包调用）
    public void setSkinTextureFromServer(ResourceLocation texture) {
        // 服务端只存储但不使用
    }

    public void setSkinStateFromServer(ZbSkinState state) {
        this.entityData.set(DATA_SKIN_STATE, state.getCode());
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::controlAnimation));
    }

    private <E extends LowerLevelZbEntity> software.bernie.geckolib.animation.PlayState controlAnimation(AnimationState<E> event) {
        // 如果正在播放 shieye 动画，继续播放
        if (this.entityData.get(DATA_PLAYING_SHIEYE)) {
            return event.setAndContinue(SHIEYE_ANIM);
        }
        
        // 如果正在攻击，播放攻击动画
        if (this.getAttackAnim(event.getPartialTick()) > 0) {
            // 攻击时有30%几率触发 shieye 动画
            if (!this.level().isClientSide && shieyeCooldown <= 0 && this.random.nextFloat() < 0.3F) {
                triggerShieyeAnimation();
                return event.setAndContinue(SHIEYE_ANIM);
            }
            return event.setAndContinue(ATTACK_ANIM);
        }
        
        // 移动时播放行走动画
        if (event.isMoving()) {
            return event.setAndContinue(WALK_ANIM);
        }
        
        // 默认播放待机动画
        return event.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void aiStep() {
        super.aiStep();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        if (playerSkinId != null) {
            compound.putUUID("PlayerSkinId", playerSkinId);
        }
        if (playerSkinName != null) {
            compound.putString("PlayerSkinName", playerSkinName);
        }
        compound.putString("CustomId", this.entityData.get(DATA_CUSTOM_ID));
        compound.putInt("SkinState", this.entityData.get(DATA_SKIN_STATE));
        compound.putBoolean("SkinLoadStarted", this.skinLoadStarted);
        compound.putBoolean("PlayingShieye", this.entityData.get(DATA_PLAYING_SHIEYE));
        compound.putInt("ShieyeCooldown", this.shieyeCooldown);
        compound.putInt("EvolutionLevel", this.evolutionLevel);
        compound.putInt("Kills", this.kills);
        compound.putInt("Hunger", this.hunger);
        compound.putInt("Variant", this.entityData.get(DATA_VARIANT));
        
        // 保存神志和贪婪系统数据
        compound.putBoolean("HasSentient", this.hasSentient);
        compound.putInt("SpeechCooldown", this.speechCooldown);
        compound.putBoolean("IsGreedy", this.isGreedy);
        compound.putInt("GreedCooldown", this.greedCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.hasUUID("PlayerSkinId")) {
            this.playerSkinId = compound.getUUID("PlayerSkinId");
        }
        if (compound.contains("PlayerSkinName")) {
            this.playerSkinName = compound.getString("PlayerSkinName");
            this.entityData.set(DATA_PLAYER_NAME, this.playerSkinName);
        }
        if (compound.contains("CustomId")) {
            this.entityData.set(DATA_CUSTOM_ID, compound.getString("CustomId"));
        }
        if (compound.contains("SkinState")) {
            this.entityData.set(DATA_SKIN_STATE, compound.getInt("SkinState"));
        }
        if (compound.contains("SkinLoadStarted")) {
            this.skinLoadStarted = compound.getBoolean("SkinLoadStarted");
        }
        if (compound.contains("PlayingShieye")) {
            this.entityData.set(DATA_PLAYING_SHIEYE, compound.getBoolean("PlayingShieye"));
        }
        if (compound.contains("ShieyeCooldown")) {
            this.shieyeCooldown = compound.getInt("ShieyeCooldown");
        }
        if (compound.contains("EvolutionLevel")) {
            this.evolutionLevel = compound.getInt("EvolutionLevel");
        }
        if (compound.contains("Kills")) {
            this.kills = compound.getInt("Kills");
        }
        if (compound.contains("Hunger")) {
            this.hunger = compound.getInt("Hunger");
        }
        if (compound.contains("Variant")) {
            this.entityData.set(DATA_VARIANT, compound.getInt("Variant"));
        }
        
        // 读取神志和贪婪系统数据
        if (compound.contains("HasSentient")) {
            this.hasSentient = compound.getBoolean("HasSentient");
        }
        if (compound.contains("SpeechCooldown")) {
            this.speechCooldown = compound.getInt("SpeechCooldown");
        }
        if (compound.contains("IsGreedy")) {
            this.isGreedy = compound.getBoolean("IsGreedy");
        }
        if (compound.contains("GreedCooldown")) {
            this.greedCooldown = compound.getInt("GreedCooldown");
        }
        
        // 读取完成后更新自定义名称
        if (!this.level().isClientSide) {
            updateCustomName();
        }
    }

    @Override
    public void tick() {
        super.tick();

        // 处理 shieye 动画状态
        if (shieyeCooldown > 0) {
            shieyeCooldown--;
        }
        
        // 处理 shieye 动画计时器
        if (!this.level().isClientSide && this.entityData.get(DATA_PLAYING_SHIEYE)) {
            shieyeAnimationTicks--;
            if (shieyeAnimationTicks <= 0) {
                this.entityData.set(DATA_PLAYING_SHIEYE, false);
            }
        }

        // 服务端：饥饿度系统
        if (!this.level().isClientSide) {
            // 每100 tick（5秒）减少1点饥饿度
            if (this.tickCount % 100 == 0 && hunger > 0) {
                hunger--;
            }
            
            // 服务端：神志系统
            if (hasSentient) {
                if (speechCooldown > 0) {
                    speechCooldown--;
                } else if (this.tickCount % 200 == 0) { // 每10秒尝试说话
                    attemptSpeech();
                }
            }
            
            // 服务端：贪婪系统
            if (isGreedy) {
                if (greedCooldown > 0) {
                    greedCooldown--;
                } else if (this.tickCount % 150 == 0) { // 每7.5秒尝试贪婪行为
                    attemptGreedyBehavior();
                }
            }
            
            // 服务端：视力随等级变化
            updateVisionRange();
        }

        // 客户端：加载皮肤
        if (this.level().isClientSide) {
            tickClient();
        }

        // 服务端：振动系统
        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            VibrationSystem.Ticker.tick(serverLevel, this.vibrationData, this.vibrationUser);
        }
    }
    
    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity entity) {
        // 如果目标是其他尸兄，检查是否应该攻击（饥饿且可以吞噬）
        if (entity instanceof LowerLevelZbEntity otherZb) {
            // 只有满足吞噬条件时才攻击其他尸兄
            if (!shouldAttackOtherZb(otherZb)) {
                return false; // 不攻击，避免无意义的互相攻击
            }
        }
        
        boolean result = super.doHurtTarget(entity);
        
        if (result && !this.level().isClientSide) {
            // 攻击时播放吃的音效
            float pitch = 0.8F + this.random.nextFloat() * 0.4F;
            this.playSound(ModSounds.GROUND_CHI.get(), 1.0F, pitch);
            
            // 随机感染村民
            if (entity instanceof net.minecraft.world.entity.npc.Villager villager) {
                if (this.random.nextFloat() < 0.3) { // 30% 概率感染
                    infectVillager(villager);
                }
            }
            
            // 击杀目标后处理进化逻辑
            handleKill(entity);
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
    
    /**
     * 判断是否应该攻击另一个尸兄
     * 只有在极度饥饿且满足吞噬条件时才攻击
     */
    private boolean shouldAttackOtherZb(LowerLevelZbEntity otherZb) {
        // 检查吞噬条件
        boolean isHungry = this.hunger <= HUNGER_THRESHOLD_FOR_CANNIBALISM;
        boolean notUnderKing = !isUnderZombieKingLeadership();
        
        // 只有在满足吞噬条件时才攻击
        return isHungry && notUnderKing;
    }
    
    private void handleKill(net.minecraft.world.entity.Entity target) {
        // 如果目标是其他尸兄，触发吞噬机制
        if (target instanceof LowerLevelZbEntity otherZb) {
            // 吞噬条件已经在 shouldAttackOtherZb 中检查过了
            // 30%概率触发吞噬
            if (this.random.nextFloat() < 0.3F) {
                CorpseOrigin.LOGGER.info("尸兄 {} 吞噬了尸兄 {}！饥饿度: {}", this.getId(), otherZb.getId(), this.hunger);
                performCannibalism(otherZb);
            }
            return; // 不增加击杀计数，吞噬单独处理
        }
        
        // 增加击杀计数
        this.kills++;
        
        // 播放吞噬效果
        this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
        
        // 检查是否可以进化
        checkEvolution();
    }
    
    /**
     * 检查是否被尸王领导
     * 尸王是进化等级5的尸兄，在32格范围内会领导其他尸兄
     */
    private boolean isUnderZombieKingLeadership() {
        if (this.level().isClientSide) return false;
        
        // 如果自己就是尸王（等级5），不需要检查
        if (this.evolutionLevel >= 5) return false;
        
        // 检查32格范围内是否有进化等级5的尸王
        return this.level().getEntitiesOfClass(LowerLevelZbEntity.class, 
            this.getBoundingBox().inflate(32.0D))
            .stream()
            .anyMatch(zb -> zb.evolutionLevel >= 5 && zb != this);
    }
    
    /**
     * 执行尸兄互相吞噬
     * 只有一个尸兄能存活并获得进化
     */
    private void performCannibalism(LowerLevelZbEntity otherZb) {
        if (this.level().isClientSide) return;
        
        // 比较两个尸兄的等级和生命值来决定谁存活
        boolean thisSurvives = shouldSurvive(otherZb);
        
        if (thisSurvives) {
            // 这个尸兄存活并吞噬另一个
            CorpseOrigin.LOGGER.info("尸兄 {} 吞噬了尸兄 {}！", this.getId(), otherZb.getId());
            
            // 播放吞噬特效
            this.level().broadcastEntityEvent(this, (byte) 35); // 粒子效果
            this.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 2.0F, 0.8F);
            
            // 获得进化收益
            int evolutionGain = Math.max(1, otherZb.getEvolutionLevel());
            this.kills += evolutionGain * 2; // 吞噬尸兄获得更多击杀数
            this.heal(this.getMaxHealth() * 0.5F); // 恢复50%生命值
            this.hunger = 100; // 恢复饥饿度到满
            
            // 立即检查进化
            checkEvolution();
            
            // 另一个尸兄死亡
            otherZb.discard();
        } else {
            // 另一个尸兄存活并吞噬这个
            CorpseOrigin.LOGGER.info("尸兄 {} 被尸兄 {} 吞噬！", this.getId(), otherZb.getId());
            
            // 播放吞噬特效
            this.level().broadcastEntityEvent(otherZb, (byte) 35);
            otherZb.playSound(net.minecraft.sounds.SoundEvents.GENERIC_EAT, 2.0F, 0.8F);
            
            // 另一个尸兄获得进化收益
            int evolutionGain = Math.max(1, this.evolutionLevel);
            otherZb.setKills(otherZb.getKills() + evolutionGain * 2);
            otherZb.heal(otherZb.getMaxHealth() * 0.5F);
            otherZb.hunger = 100; // 恢复饥饿度到满
            otherZb.checkEvolution();
            
            // 这个尸兄死亡
            this.discard();
        }
    }
    
    /**
     * 判断这个尸兄是否应该在与另一个尸兄的吞噬中存活
     */
    private boolean shouldSurvive(LowerLevelZbEntity other) {
        // 优先比较进化等级
        if (this.evolutionLevel != other.getEvolutionLevel()) {
            return this.evolutionLevel > other.getEvolutionLevel();
        }
        
        // 等级相同，比较当前生命值
        if (this.getHealth() != other.getHealth()) {
            return this.getHealth() > other.getHealth();
        }
        
        // 生命值也相同，随机决定
        return this.random.nextBoolean();
    }
    
    private void checkEvolution() {
        int requiredKills = this.evolutionLevel * 3; // 每个等级需要3次击杀
        
        if (this.kills >= requiredKills && this.evolutionLevel < 5) {
            evolve();
        }
    }
    
    private void evolve() {
        this.evolutionLevel++;
        this.kills = 0;
        
        // 增加属性
        double healthBonus = this.evolutionLevel * 5.0;
        double damageBonus = this.evolutionLevel * 1.0;
        double speedBonus = this.evolutionLevel * 0.05;
        
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(20.0 + healthBonus);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(3.0 + damageBonus);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.3 + speedBonus);
        
        this.setHealth(this.getMaxHealth());
        
        // 播放进化效果
        this.level().broadcastEntityEvent(this, (byte) 20);
        this.playSound(net.minecraft.sounds.SoundEvents.ILLUSIONER_PREPARE_BLINDNESS, 1.5F, 0.8F);
        
        CorpseOrigin.LOGGER.info("尸兄进化到 {} 级！", this.evolutionLevel);
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        String name = this.entityData.get(DATA_PLAYER_NAME);
        int stateCode = this.entityData.get(DATA_SKIN_STATE);
        ZbSkinState currentState = ZbSkinState.fromCode(stateCode);

        // 如果有玩家名
        if (name != null && !name.isEmpty()) {
            // 情况1：未开始加载，且当前状态允许加载
            if (!skinLoadStarted && (currentState == ZbSkinState.NOT_LOADED || currentState == ZbSkinState.FAILED)) {
                startSkinLoad(name);
            }
            // 情况2：皮肤状态是LOADED但皮肤纹理为null（重启后会出现）
            else if (currentState == ZbSkinState.LOADED && this.skinTexture == null) {
                // 重新加载皮肤
                startSkinLoad(name);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void startSkinLoad(String playerName) {
        // 即使已经开始加载，也要重新加载（用于重启后皮肤为null的情况）
        skinLoadStarted = true;

        // 设置状态为加载中
        this.entityData.set(DATA_SKIN_STATE, ZbSkinState.LOADING.getCode());

        // 异步加载皮肤
        ZbSkinLoader.loadSkinAsync(this, playerName);
    }

    /**
     * 设置皮肤纹理（客户端调用）
     */
    @OnlyIn(Dist.CLIENT)
    public void setSkinTexture(ResourceLocation texture) {
        this.skinTexture = texture;
    }

    /**
     * 获取皮肤纹理（渲染器调用）
     */
    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getSkinTexture() {
        return this.skinTexture;
    }

    /**
     * 设置皮肤状态（客户端调用后通知服务端）
     */
    @OnlyIn(Dist.CLIENT)
    public void setSkinState(ZbSkinState state) {
        this.entityData.set(DATA_SKIN_STATE, state.getCode());

        // 发送网络包通知服务端
        if (!(this.level() instanceof ServerLevel)) {
            try {
                PacketDistributor.sendToServer(new ZbSkinUpdatePacket(
                        this.getId(),
                        this.skinTexture,
                        state.getCode()
                ));
            } catch (Exception e) {
                CorpseOrigin.LOGGER.error("发送皮肤更新包失败", e);
            }
        }
    }

    /**
     * 获取皮肤状态（服务端/客户端均可）
     */
    public ZbSkinState getSkinState() {
        return ZbSkinState.fromCode(this.entityData.get(DATA_SKIN_STATE));
    }

    public UUID getPlayerSkinId() {
        return playerSkinId;
    }

    public String getPlayerSkinName() {
        return this.entityData.get(DATA_PLAYER_NAME);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence();
    }

    /**
     * 更新实体的自定义名称（用于玉模组和头顶标签）
     */
    private void updateCustomName() {
        String customId = this.getCustomId();
        String playerName = this.getPlayerSkinName();

        net.minecraft.network.chat.Component displayName;

        if (!customId.isEmpty()) {
            displayName = net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.named",
                    customId
            );
        } else if (playerName != null && !playerName.isEmpty()) {
            displayName = net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.named",
                    playerName
            );
        } else {
            displayName = net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.default"
            );
        }

        // 设置自定义名称
        this.setCustomName(displayName);
        // 确保始终显示（可选）
        this.setCustomNameVisible(false);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        String customId = this.getCustomId();
        String playerName = this.getPlayerSkinName();

        if (!customId.isEmpty()) {
            // 有自定义ID时：使用 entity.corpseorigin.lower_level_zb.named
            return net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.named",
                    customId
            );
        } else if (playerName != null && !playerName.isEmpty()) {
            // 有玩家皮肤名称时：也使用 entity.corpseorigin.lower_level_zb.named
            return net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.named",
                    playerName
            );
        } else {
            // 默认显示：使用 entity.corpseorigin.lower_level_zb.default
            return net.minecraft.network.chat.Component.translatable(
                    "entity.corpseorigin.lower_level_zb.default"
            );
        }
    }
    
    public int getEvolutionLevel() {
        return this.evolutionLevel;
    }
    
    public int getKills() {
        return this.kills;
    }
    
    public void setEvolutionLevel(int level) {
        this.evolutionLevel = Math.max(1, Math.min(5, level));
    }
    
    public void setKills(int kills) {
        this.kills = Math.max(0, kills);
    }
    
    /**
     * 触发 shieye 动画
     */
    private void triggerShieyeAnimation() {
        this.entityData.set(DATA_PLAYING_SHIEYE, true);
        this.shieyeCooldown = 200; // 10秒冷却时间
        this.shieyeAnimationTicks = 60; // 3秒 = 60 ticks，动画持续时间
    }
    
    /**
     * 更新视力范围
     * 基础尸兄在晚上和人类一样的视力
     * 随着等级提升，视力范围增加
     */
    private void updateVisionRange() {
        double baseVision = 16.0D; // 人类基础视力
        double levelBonus = this.evolutionLevel * 2.0D; // 每级增加2格视力
        double totalVision = baseVision + levelBonus;
        
        // 更新视力属性
        if (this.getAttribute(Attributes.FOLLOW_RANGE) != null) {
            this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(totalVision);
        }
    }
    
    /**
     * 尝试说话
     * 保留生前神志的尸兄会随机口吐人言
     */
    private void attemptSpeech() {
        if (this.random.nextFloat() < 0.3) { // 30%概率说话
            String[] phrases = {
                "救...救我...",
                "好饿...",
                "我...我怎么了...",
                "不要...不要杀我...",
                "肉...肉...",
                "谁来...救救我...",
                "好疼...好疼...",
                "我不想死..."
            };
            
            String phrase = phrases[this.random.nextInt(phrases.length)];
            this.level().broadcastEntityEvent(this, (byte) 46); // 播放说话效果
            
            // 发送聊天消息（只有附近的玩家能看到）
            net.minecraft.network.chat.Component message = net.minecraft.network.chat.Component.literal(phrase);
            for (Player player : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(32.0D))) {
                player.sendSystemMessage(message);
            }
            
            speechCooldown = 400; // 20秒冷却
        }
    }
    
    /**
     * 尝试贪婪行为
     * 贪婪的尸兄会被黄金和绿宝石吸引
     */
    private void attemptGreedyBehavior() {
        // 搜索附近的黄金和绿宝石物品
        java.util.List<net.minecraft.world.entity.item.ItemEntity> items = this.level().getEntitiesOfClass(
            net.minecraft.world.entity.item.ItemEntity.class,
            this.getBoundingBox().inflate(16.0D)
        );
        
        net.minecraft.world.entity.item.ItemEntity targetItem = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (net.minecraft.world.entity.item.ItemEntity item : items) {
            net.minecraft.world.item.ItemStack stack = item.getItem();
            if (isValuableItem(stack)) {
                double distance = this.distanceToSqr(item);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    targetItem = item;
                }
            }
        }
        
        // 如果找到有价值的物品，靠近它
        if (targetItem != null) {
            this.getNavigation().moveTo(targetItem, 1.2D);
            greedCooldown = 200; // 10秒冷却
        }
    }
    
    /**
     * 判断物品是否有价值（黄金或绿宝石）
     */
    private boolean isValuableItem(net.minecraft.world.item.ItemStack stack) {
        net.minecraft.world.item.Item item = stack.getItem();
        return item == net.minecraft.world.item.Items.GOLD_INGOT ||
               item == net.minecraft.world.item.Items.GOLD_NUGGET ||
               item == net.minecraft.world.item.Items.GOLD_BLOCK ||
               item == net.minecraft.world.item.Items.EMERALD ||
               item == net.minecraft.world.item.Items.EMERALD_BLOCK ||
               item == net.minecraft.world.item.Items.GOLDEN_APPLE ||
               item == net.minecraft.world.item.Items.ENCHANTED_GOLDEN_APPLE;
    }
}