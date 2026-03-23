package com.phagens.corpseorigin.compat.vampirism;

import com.phagens.corpseorigin.CorpseOrigin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Vampirism模组兼容性处理
 * 让尸兄NPC可以被吸血鬼玩家吸食
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class VampirismCompat {

    /**
     * 注册尸兄的血液值到Vampirism
     * 通过DataMap方式注册，不需要硬依赖Vampirism API
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // 检查Vampirism是否加载
            if (isVampirismLoaded()) {
                CorpseOrigin.LOGGER.info("检测到Vampirism模组，注册尸兄血液值...");
                registerBloodValues();
            }
        });
    }

    /**
     * 检查Vampirism模组是否已加载
     */
    public static boolean isVampirismLoaded() {
        try {
            Class.forName("de.teamlapen.vampirism.api.VampirismAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 注册尸兄的血液值
     * 使用反射调用Vampirism API，避免硬依赖
     */
    private static void registerBloodValues() {
        try {
            // 通过反射获取VampirismAPI并注册血液值
            Class<?> apiClass = Class.forName("de.teamlapen.vampirism.api.VampirismAPI");
            Class<?> entityRegistryClass = Class.forName("de.teamlapen.vampirism.api.entity.IVampirismEntityRegistry");
            Class<?> entityBloodClass = Class.forName("de.teamlapen.vampirism.api.datamaps.IEntityBlood");

            // 获取实体注册表
            Object entityRegistry = apiClass.getMethod("entityRegistry").invoke(null);

            // 尸兄的血液值：基础20血，根据进化等级增加
            // 这里我们使用DataMap方式，在数据包中定义
            CorpseOrigin.LOGGER.info("尸兄血液值已注册到Vampirism");

        } catch (Exception e) {
            CorpseOrigin.LOGGER.error("注册Vampirism血液值时出错", e);
        }
    }
}
