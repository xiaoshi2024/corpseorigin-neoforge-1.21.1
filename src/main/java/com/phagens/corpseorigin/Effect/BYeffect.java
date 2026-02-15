package com.phagens.corpseorigin.Effect;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class BYeffect extends MobEffect {

    public BYeffect(MobEffectCategory category, int color) {
        super(category, color);
    }


//备选接触变异
//    @Override
//    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
//        super.onEffectAdded(livingEntity, amplifier);
//        if (!livingEntity.level().isClientSide && livingEntity.level() instanceof ServerLevel serverLevel) {
//            performTransformation(livingEntity, serverLevel);
//        }
//    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide && livingEntity.level() instanceof ServerLevel serverLevel) {
            performTransformation(livingEntity, serverLevel);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration==20;
    }

    private void performTransformation(LivingEntity livingEntity, ServerLevel serverLevel) {
        EntityType<?> targetType = getTransformationTarget(livingEntity);
        if (targetType != null){
            BlockPos pos = livingEntity.blockPosition();
            float yRot = livingEntity.getYRot();
            float xRot = livingEntity.getXRot();
            livingEntity.remove(Entity.RemovalReason.CHANGED_DIMENSION);

            LivingEntity newEntity = (LivingEntity) targetType.create(serverLevel);
            if (newEntity != null){
                newEntity.moveTo(pos.getX(), pos.getY(), pos.getZ(), yRot, xRot);
                serverLevel.addFreshEntity(newEntity);
            }
        }
    }

    private EntityType<?> getTransformationTarget(LivingEntity livingEntity) {
        EntityType<?> type = livingEntity.getType();//暂定
        if (type == EntityType.PIG) return EntityType.COW;
        if (type == EntityType.SHEEP) return EntityType.PIG;
        if (type == EntityType.COW) return EntityType.SHEEP;
        if (type == EntityType.ZOMBIE) return EntityType.SKELETON;
        if (type == EntityType.SKELETON) return EntityType.ZOMBIE;
        if (type == EntityType.CREEPER) return EntityType.ENDERMAN;
        return null;
    }


}
