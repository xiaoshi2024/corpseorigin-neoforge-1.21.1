package com.phagens.corpseorigin.Effect;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.network.PlayerCorpseSyncPacket;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import com.phagens.corpseorigin.register.EffectRegister;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class BYeffect extends MobEffect {

    public BYeffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        super.onEffectAdded(livingEntity, amplifier);
        if (!livingEntity.level().isClientSide) {
            CorpseOrigin.LOGGER.info("感染效果添加到 {}，将在效果结束时变异",
                    livingEntity.getName().getString());
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 只在效果的最后1 tick返回true，这样applyEffectTick会在最后1 tick执行
        return duration == 1;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // 这个方法会在shouldApplyEffectTickThisTick返回true时执行
        // 在效果的最后1 tick执行转化
        if (!livingEntity.level().isClientSide && livingEntity.level() instanceof ServerLevel serverLevel) {
            performTransformation(livingEntity, serverLevel);
        }
        return true;
    }

    /**
     * 执行转化逻辑
     */
    private void performTransformation(LivingEntity livingEntity, ServerLevel serverLevel) {
        // 只处理村民转化为尸兄
        if (livingEntity instanceof Villager villager) {
            convertVillagerToZb(villager, serverLevel);
        }
        // 玩家感染逻辑 - 转化为尸族
        else if (livingEntity instanceof ServerPlayer player) {
            convertPlayerToCorpse(player);
        }
    }

    /**
     * 将村民转化为尸兄
     */
    private void convertVillagerToZb(Villager villager, ServerLevel serverLevel) {
        // 检查实体是否已注册
        if (EntityRegistry.LOWER_LEVEL_ZB.get() == null) {
            CorpseOrigin.LOGGER.error("LOWER_LEVEL_ZB 实体未注册！");
            return;
        }

        try {
            // 创建尸兄实体
            LowerLevelZbEntity zb = new LowerLevelZbEntity(EntityRegistry.LOWER_LEVEL_ZB.get(), serverLevel);
            zb.setPos(villager.getX(), villager.getY(), villager.getZ());
            zb.setYRot(villager.getYRot());
            zb.setXRot(villager.getXRot());

            // 随机设置变种类型，30%概率为裂口尸兄
            if (serverLevel.getRandom().nextFloat() < 0.3) {
                zb.setVariant(LowerLevelZbEntity.Variant.CRACKED);
            }

            // 设置皮肤为村民的名字（如果有）
            String villagerName = villager.getName().getString();
            zb.setPlayerSkinName(villagerName);

            // 复制村民的一些属性（可选）
            zb.setCustomName(villager.getCustomName());
            zb.setCustomNameVisible(villager.isCustomNameVisible());

            // 移除村民
            villager.remove(Entity.RemovalReason.CHANGED_DIMENSION);

            // 添加尸兄到世界
            serverLevel.addFreshEntity(zb);

            // 播放转化特效
            serverLevel.broadcastEntityEvent(zb, (byte) 35);

            CorpseOrigin.LOGGER.info("村民 {} 成功变异为尸兄", villagerName);
        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("村民转化失败: {}", e.getMessage());
        }
    }

    /**
     * 静态方法：给实体添加感染效果（带随机延迟）
     */
    public static void applyInfection(LivingEntity target, ServerLevel serverLevel) {
        if (target == null || serverLevel == null) return;

        // 检查目标是否已经被感染，避免重复感染
        if (target.hasEffect(EffectRegister.QIANS)) {
            CorpseOrigin.LOGGER.debug("目标 {} 已经被感染，跳过重复感染",
                    target.getName().getString());
            return;
        }

        // 检查玩家是否已经是尸兄，避免重复感染
        if (target instanceof Player player && PlayerCorpseData.isCorpse(player)) {
            CorpseOrigin.LOGGER.debug("玩家 {} 已经是尸兄，跳过感染",
                    player.getName().getString());
            return;
        }

        // 随机延迟3-15秒（60-300 ticks）
        int duration = 60 + serverLevel.getRandom().nextInt(241);

        // 添加效果 - 使用 EffectRegister.QIANS
        target.addEffect(new MobEffectInstance(
                EffectRegister.QIANS,  // 使用注册的效果 Holder
                duration,
                0,                      // 放大器
                false,                  // 不显示粒子
                true,                   // 显示图标
                true
        ));

        CorpseOrigin.LOGGER.info("感染效果已应用到 {}，将在 {} 秒后变异",
                target.getName().getString(), duration / 20);
    }

    /**
     * 静态方法：给实体添加感染效果（自定义延迟）
     */
    public static void applyInfection(LivingEntity target, ServerLevel serverLevel, int durationTicks) {
        if (target == null || serverLevel == null) return;

        // 检查目标是否已经被感染，避免重复感染
        if (target.hasEffect(EffectRegister.QIANS)) {
            CorpseOrigin.LOGGER.debug("目标 {} 已经被感染，跳过重复感染",
                    target.getName().getString());
            return;
        }

        // 检查玩家是否已经是尸兄，避免重复感染
        if (target instanceof Player player && PlayerCorpseData.isCorpse(player)) {
            CorpseOrigin.LOGGER.debug("玩家 {} 已经是尸兄，跳过感染",
                    player.getName().getString());
            return;
        }

        // 添加效果 - 使用 EffectRegister.QIANS
        target.addEffect(new MobEffectInstance(
                EffectRegister.QIANS,  // 使用注册的效果 Holder
                durationTicks,
                0,
                false,
                true,
                true
        ));

        CorpseOrigin.LOGGER.info("感染效果已应用到 {}，将在 {} 秒后变异",
                target.getName().getString(), durationTicks / 20);
    }

    /**
     * 将玩家转化为尸族
     */
    private void convertPlayerToCorpse(ServerPlayer player) {
        // 设置玩家为尸族状态
        PlayerCorpseData.setPlayerAsCorpse(player, 1);

        // 同步到客户端
        PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                player.getId(), true, 1, PlayerCorpseData.getCorpseData(player)
        );
        PacketDistributor.sendToPlayer(player, packet);
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);

        // 播放转化特效
        player.level().broadcastEntityEvent(player, (byte) 35);

        CorpseOrigin.LOGGER.info("玩家 {} 已转化为尸族！", player.getName().getString());
    }

    /**
     * 检查目标是否可以被感染
     * 已经是尸兄的玩家不会被感染
     */
    public static boolean canInfect(LivingEntity target) {
        // 村民可以被感染
        if (target instanceof Villager) {
            return true;
        }
        
        // 玩家可以被感染，但已经是尸兄的玩家除外
        if (target instanceof Player player) {
            return !PlayerCorpseData.isCorpse(player);
        }
        
        return false;
    }
}