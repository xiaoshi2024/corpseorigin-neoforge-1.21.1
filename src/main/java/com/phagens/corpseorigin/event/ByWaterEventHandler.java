package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Block.custom.QiXingGuan;
import com.phagens.corpseorigin.data.InfectionData;
import com.phagens.corpseorigin.register.EffectRegister;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.FluidTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class ByWaterEventHandler {
    private static final Map<UUID, Long> playerPoisonCooldowns = new HashMap<>();
    private static final Map<UUID, Long> playerCheckCooldowns = new HashMap<>();
    private static final long POISON_COOLDOWN = 3000;
    private static final int POISON_DURATION = 60;
    private static final int POISON_AMPLIFIER = 0;
    private static final long CHECK_INTERVAL = 500;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide) {
            return;
        }

        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastCheckTime = playerCheckCooldowns.get(playerUUID);

        if (lastCheckTime == null || (currentTime - lastCheckTime) >= CHECK_INTERVAL) {
            BlockPos playerPos = player.blockPosition();
            
            if (isPlayerInInfectedWater(level, playerPos)) {
                applyPoisonEffect(player);
            }
            
            playerCheckCooldowns.put(playerUUID, currentTime);
        }
    }

    // 方法1：使用 EntityTickEvent（推荐）
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity entity && !(entity instanceof Player)) {
            Level level = entity.level();
            if (level.isClientSide) return;
            // 处理非玩家生物的逻辑
            handleLivingEntityInfection(entity, level);
        }

    }

    private static void handleLivingEntityInfection(LivingEntity entity, Level level) {
        UUID entityUUID = entity.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastCheckTime = playerCheckCooldowns.get(entityUUID); // 使用统一的检查冷却

        if (lastCheckTime == null || (currentTime - lastCheckTime) >= CHECK_INTERVAL) {
            BlockPos entityPos = entity.blockPosition();

            if (isEntityInInfectedWater(level, entityPos)) {
                applyPoisonEffectToEntity(entity);
            }

            playerCheckCooldowns.put(entityUUID, currentTime); // 使用统一的冷却映射
        }
    }

    private static void applyPoisonEffectToEntity(LivingEntity entity) {
        UUID entityUUID = entity.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastPoisonTime = playerPoisonCooldowns.get(entityUUID); // 使用统一的中毒冷却

        if (lastPoisonTime == null || (currentTime - lastPoisonTime) >= POISON_COOLDOWN) {
            entity.addEffect(new MobEffectInstance(
                    EffectRegister.QIANS,
                    POISON_DURATION,
                    POISON_AMPLIFIER
            ));
            playerPoisonCooldowns.put(entityUUID, currentTime); // 使用统一的冷却映射
        }
    }

    private static boolean isEntityInInfectedWater(Level level, BlockPos entityPos) {
        BlockState blockState = level.getBlockState(entityPos);
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            if (level instanceof ServerLevel serverLevel) {
                return InfectionData.isWaterInfectedStatic(serverLevel, entityPos);
            }
        }
        return false;
    }


    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player && event.getLevel() instanceof ServerLevel serverLevel) {
            BlockState state = event.getPlacedBlock();
            if (state.getFluidState().is(FluidTags.WATER)) {
                BlockPos pos = event.getPos();
                if (isNextToInfectedWater(serverLevel, pos)) {
                    InfectionData.markWaterInfectedStatic(serverLevel, pos);
                }
            }
        }

    }

    private static boolean isPlayerInInfectedWater(Level level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.getFluidState().is(FluidTags.WATER)) {
            if (level instanceof ServerLevel serverLevel) {
                return InfectionData.isWaterInfectedStatic(serverLevel, pos);
            }
        }
        return false;
    }

    private static boolean isNextToInfectedWater(ServerLevel level, BlockPos pos) {
        // 限制同化范围为1格（直接相邻）
        final int MAX_RANGE = 1;
        
        for (int x = -MAX_RANGE; x <= MAX_RANGE; x++) {
            for (int y = -MAX_RANGE; y <= MAX_RANGE; y++) {
                for (int z = -MAX_RANGE; z <= MAX_RANGE; z++) {
                    // 跳过自身
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    // 计算曼哈顿距离，确保在范围内
                    int distance = Math.abs(x) + Math.abs(y) + Math.abs(z);
                    if (distance <= MAX_RANGE) {
                        BlockPos neighbor = pos.offset(x, y, z);
                        if (InfectionData.isWaterInfectedStatic(level, neighbor)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static void applyPoisonEffect(Player player) {
        UUID playerUUID = player.getUUID();
        long currentTime = System.currentTimeMillis();
        Long lastPoisonTime = playerPoisonCooldowns.get(playerUUID);

        if (lastPoisonTime == null || (currentTime - lastPoisonTime) >= POISON_COOLDOWN) {
            player.addEffect(new MobEffectInstance(
                MobEffects.POISON,
                POISON_DURATION,
                POISON_AMPLIFIER
            ));
            playerPoisonCooldowns.put(playerUUID, currentTime);
        }
    }
}