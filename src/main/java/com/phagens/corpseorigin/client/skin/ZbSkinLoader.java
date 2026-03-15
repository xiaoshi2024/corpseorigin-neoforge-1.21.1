package com.phagens.corpseorigin.client.skin;

import com.mojang.logging.LogUtils;
import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.slf4j.Logger;

/**
 * 皮肤加载器 - 处理异步皮肤加载
 */
@OnlyIn(Dist.CLIENT)
public class ZbSkinLoader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 为尸兄实体异步加载皮肤
     */
    public static void loadSkinAsync(LowerLevelZbEntity entity, String username) {
        if (username == null || username.isEmpty()) {
            entity.setSkinState(ZbSkinState.FAILED);
            return;
        }

        // 先检查缓存
        ResourceLocation cached = ZbSkinCache.get(username);
        if (cached != null) {
            entity.setSkinTexture(cached);
            entity.setSkinState(ZbSkinState.LOADED);
            LOGGER.debug("✅ 从缓存加载皮肤: {}", username);
            return;
        }

        // 设置为加载中状态
        entity.setSkinState(ZbSkinState.LOADING);

        // 异步加载皮肤
        Thread skinThread = new Thread(() -> {
            try {
                LOGGER.info("🔍 开始加载皮肤: {}", username);

                // 优先使用 CustomSkinLoader
                ResourceLocation skin = null;
                if (ZbSkinIntegration.isCslAvailable()) {
                    skin = ZbSkinIntegration.getPlayerSkin(username);
                }

                final ResourceLocation finalSkin = skin;

                // 回到主线程更新实体
                Minecraft.getInstance().execute(() -> {
                    if (finalSkin != null) {
                        // 加载成功
                        entity.setSkinTexture(finalSkin);
                        entity.setSkinState(ZbSkinState.LOADED);
                        ZbSkinCache.put(username, finalSkin);
                        LOGGER.info("✅ 皮肤加载成功: {}", username);
                    } else {
                        // 加载失败
                        entity.setSkinState(ZbSkinState.FAILED);
                        LOGGER.warn("❌ 皮肤加载失败: {}", username);
                    }
                });

            } catch (Exception e) {
                LOGGER.error("皮肤加载异常: {}", e.getMessage());
                Minecraft.getInstance().execute(() -> {
                    entity.setSkinState(ZbSkinState.FAILED);
                });
            }
        });

        skinThread.setDaemon(true);
        skinThread.setName("ZbSkinLoader-" + username);
        skinThread.start();
    }
}