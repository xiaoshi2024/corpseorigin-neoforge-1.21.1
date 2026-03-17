//package com.phagens.corpseorigin.client.Models.entity;
//
//import com.phagens.corpseorigin.CorpseOrigin;
//import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
//import com.phagens.corpseorigin.player.PlayerCorpseData;
//import net.minecraft.client.player.AbstractClientPlayer;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.syncher.SynchedEntityData;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.EntityType;
//import software.bernie.geckolib.animatable.GeoAnimatable;
//import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
//import software.bernie.geckolib.animation.*;
//import software.bernie.geckolib.model.GeoModel;
//import software.bernie.geckolib.util.GeckoLibUtil;
//
///**
// * 尸族玩家Geo模型 - 复用 LowerLevelZbEntity 的模型和动画
// */
//public class CorpsePlayerGeoModel extends GeoModel<CorpsePlayerGeoModel.CorpsePlayerProxy> {
//
//    // 复用 LowerLevelZbEntity 的模型资源
//    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(
//            CorpseOrigin.MODID, "geo/entity/lower_level_zb.geo.json");
//    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(
//            CorpseOrigin.MODID, "animations/entity/lower_level_zb.animation.json");
//
//    // 默认纹理（会被渲染层覆盖）
//    private static final ResourceLocation DEFAULT_TEXTURE = ResourceLocation.fromNamespaceAndPath(
//            CorpseOrigin.MODID, "textures/entity/lower_level_zb_render.png");
//
//    @Override
//    public ResourceLocation getModelResource(CorpsePlayerProxy proxy) {
//        return MODEL;
//    }
//
//    @Override
//    public ResourceLocation getTextureResource(CorpsePlayerProxy proxy) {
//        return DEFAULT_TEXTURE;
//    }
//
//    @Override
//    public ResourceLocation getAnimationResource(CorpsePlayerProxy proxy) {
//        return ANIMATION;
//    }
//
//    /**
//     * 代理类 - 包装AbstractClientPlayer以实现GeoAnimatable
//     * 完全模拟 LowerLevelZbEntity 的动画行为
//     */
//    public static class CorpsePlayerProxy extends Entity implements GeoAnimatable {
//
//        // 复用 LowerLevelZbEntity 的动画定义
//        protected static final RawAnimation WALK_ANIM = RawAnimation.begin().thenLoop("walk");
//        protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
//        protected static final RawAnimation ATTACK_ANIM = RawAnimation.begin().thenPlay("attack");
//        protected static final RawAnimation SHIEYE_ANIM = RawAnimation.begin().thenPlay("shieye");
//
//        private final AbstractClientPlayer player;
//        private final int variant;
//        private final AnimatableInstanceCache cache;
//
//        // 动画状态跟踪
//        private int shieyeCooldown = 0;
//        private boolean playingShieye = false;
//
//        public CorpsePlayerProxy(AbstractClientPlayer player) {
//            super(EntityType.PLAYER, player.level());
//            this.player = player;
//            this.variant = PlayerCorpseData.getVariant(player);
//            this.cache = GeckoLibUtil.createInstanceCache(this);
//
//            updatePositionAndRotation();
//        }
//
//        @Override
//        protected void defineSynchedData(SynchedEntityData.Builder builder) {}
//
//        @Override
//        protected void readAdditionalSaveData(CompoundTag tag) {}
//
//        @Override
//        protected void addAdditionalSaveData(CompoundTag tag) {}
//
//        public AbstractClientPlayer getPlayer() {
//            return player;
//        }
//
//        public int getVariant() {
//            return variant;
//        }
//
//        public void updatePositionAndRotation() {
//            this.setPos(player.getX(), player.getY(), player.getZ());
//            this.setRot(player.getYRot(), player.getXRot());
//        }
//
//        @Override
//        public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
//            // 注册动画控制器，与 LowerLevelZbEntity 保持一致
//            controllers.add(new AnimationController<>(this, "controller", 5, this::controlAnimation));
//        }
//
//        private <E extends CorpsePlayerProxy> PlayState controlAnimation(AnimationState<E> event) {
//            // 如果正在播放 shieye 动画，继续播放
//            if (playingShieye) {
//                return event.setAndContinue(SHIEYE_ANIM);
//            }
//
//            // 更新冷却
//            if (shieyeCooldown > 0) {
//                shieyeCooldown--;
//            }
//
//            // 如果正在攻击，播放攻击动画
//            if (player.swinging) {
//                // 攻击时有30%几率触发 shieye 动画
//                if (shieyeCooldown <= 0 && player.getRandom().nextFloat() < 0.3F) {
//                    triggerShieyeAnimation();
//                    return event.setAndContinue(SHIEYE_ANIM);
//                }
//                return event.setAndContinue(ATTACK_ANIM);
//            }
//
//            // 移动时播放行走动画
//            if (player.walkAnimation.speed() > 0.01F || player.walkAnimation.isMoving()) {
//                return event.setAndContinue(WALK_ANIM);
//            }
//
//            // 默认播放待机动画
//            return event.setAndContinue(IDLE_ANIM);
//        }
//
//        private void triggerShieyeAnimation() {
//            playingShieye = true;
//            shieyeCooldown = 200; // 10秒冷却时间
//        }
//
//        public void tick() {
//            // 处理 shieye 动画结束
//            if (playingShieye) {
//                // 简单实现：这里可以在动画完成后重置
//                // 实际应该通过动画监听器来处理
//            }
//        }
//
//        @Override
//        public AnimatableInstanceCache getAnimatableInstanceCache() {
//            return cache;
//        }
//
//        @Override
//        public double getTick(Object object) {
//            return player.tickCount;
//        }
//    }
//}