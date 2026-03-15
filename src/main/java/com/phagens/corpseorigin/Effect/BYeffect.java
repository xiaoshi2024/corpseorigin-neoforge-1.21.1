package com.phagens.corpseorigin.Effect;

import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;

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
        // 只处理村民转化为尸兄
        if (livingEntity instanceof Villager villager) {
            convertVillagerToZb(villager, serverLevel);
        }
    }

    /**
     * 将村民转化为尸兄
     */
    private void convertVillagerToZb(Villager villager, ServerLevel serverLevel) {
        // 创建尸兄实体
        LowerLevelZbEntity zb = new LowerLevelZbEntity(EntityRegistry.LOWER_LEVEL_ZB.get(), serverLevel);
        zb.setPos(villager.getX(), villager.getY(), villager.getZ());
        zb.setYRot(villager.getYRot());
        zb.setXRot(villager.getXRot());
        
        // 设置皮肤为村民的名字（如果有）
        String villagerName = villager.getName().getString();
        if (!villagerName.equals("村民")) {
            zb.setPlayerSkinName(villagerName);
        }
        
        // 移除村民
        villager.remove(Entity.RemovalReason.CHANGED_DIMENSION);
        
        // 添加尸兄到世界
        serverLevel.addFreshEntity(zb);
        
        // 播放转化特效
        serverLevel.broadcastEntityEvent(zb, (byte) 35);
    }


}
