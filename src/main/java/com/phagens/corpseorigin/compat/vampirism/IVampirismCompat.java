package com.phagens.corpseorigin.compat.vampirism;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.world.entity.PathfinderMob;

/**
 * Vampirism兼容性接口
 * 通过反射调用Vampirism API，避免硬依赖
 */
public class IVampirismCompat {

    private static final String VAMPIRE_API_CLASS = "de.teamlapen.vampirism.api.VampirismAPI";
    private static final String VAMPIRE_INTERFACE = "de.teamlapen.vampirism.api.entity.vampire.IVampire";

    /**
     * 检查Vampirism是否已加载
     */
    public static boolean isVampirismLoaded() {
        try {
            Class.forName(VAMPIRE_API_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 检查对象是否是吸血鬼
     */
    public static boolean isVampire(Object obj) {
        if (!isVampirismLoaded() || obj == null) return false;
        try {
            Class<?> vampireInterface = Class.forName(VAMPIRE_INTERFACE);
            return vampireInterface.isInstance(obj);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 获取实体的血液值（用于Vampirism显示）
     * @param entity 尸兄实体
     * @return 当前血液值
     */
    public static int getBlood(PathfinderMob entity) {
        // 尸兄的血液值基于等级
        if (entity instanceof com.phagens.corpseorigin.entity.LowerLevelZbEntity zb) {
            return 15 + zb.getEvolutionLevel() * 5;
        }
        return 20;
    }

    /**
     * 获取最大血液值
     * @param entity 尸兄实体
     * @return 最大血液值
     */
    public static int getMaxBlood(PathfinderMob entity) {
        if (entity instanceof com.phagens.corpseorigin.entity.LowerLevelZbEntity zb) {
            return 15 + zb.getEvolutionLevel() * 5;
        }
        return 20;
    }

    /**
     * 当被吸血鬼吸食时调用
     * @param entity 被吸食的尸兄
     * @param biter 吸血鬼
     * @return 吸食的血液量
     */
    public static int onBite(PathfinderMob entity, Object biter) {
        if (!(entity instanceof com.phagens.corpseorigin.entity.LowerLevelZbEntity zb)) {
            return 0;
        }

        CorpseOrigin.LOGGER.debug("尸兄 {} 被吸血鬼吸食", entity.getId());

        // 尸兄的血液值
        int maxBlood = getMaxBlood(entity);
        int currentBlood = maxBlood; // 简化处理，假设总是满的

        // 计算吸食量
        int amount = Math.max(1, maxBlood / 4);

        // 对尸兄造成伤害
        entity.hurt(entity.damageSources().generic(), amount);

        CorpseOrigin.LOGGER.info("吸血鬼从尸兄 {} 吸食了 {} 点血液", entity.getId(), amount);

        return amount;
    }

    /**
     * 检查是否可以被吸食
     * @param entity 尸兄实体
     * @param biter 吸血鬼
     * @return 是否可以被吸食
     */
    public static boolean canBeBitten(PathfinderMob entity, Object biter) {
        if (!(entity instanceof com.phagens.corpseorigin.entity.LowerLevelZbEntity)) {
            return false;
        }
        // 尸兄总是可以被吸食（只要有血）
        return entity.isAlive();
    }
}
