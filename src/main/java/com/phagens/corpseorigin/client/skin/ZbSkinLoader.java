package com.phagens.corpseorigin.client.skin;

import com.mojang.logging.LogUtils;
import com.phagens.corpseorigin.entity.LowerLevelZbEntity;
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
            // 使用默认皮肤
            ResourceLocation defaultSkin = getDefaultSkin(username);
            entity.setSkinTexture(defaultSkin);
            entity.setSkinState(ZbSkinState.LOADED);
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

                // 如果 CSL 不可用或加载失败，使用默认皮肤
                if (skin == null) {
                    skin = getDefaultSkin(username);
                    LOGGER.info("⚠️ 使用默认皮肤: {}", username);
                }

                final ResourceLocation finalSkin = skin;

                // 回到主线程更新实体
                ResourceLocation finalSkin1 = skin;
                Minecraft.getInstance().execute(() -> {
                    entity.setSkinTexture(finalSkin);
                    entity.setSkinState(ZbSkinState.LOADED);
                    
                    // 即使是默认皮肤也缓存
                    ZbSkinCache.put(username, finalSkin);
                    
                    if (finalSkin1 != getDefaultSkin(username)) {
                        LOGGER.info("✅ 皮肤加载成功: {}", username);
                    }
                });

            } catch (Exception e) {
                LOGGER.error("皮肤加载异常: {}", e.getMessage());
                Minecraft.getInstance().execute(() -> {
                    // 异常时使用默认皮肤
                    ResourceLocation defaultSkin = getDefaultSkin(username);
                    entity.setSkinTexture(defaultSkin);
                    entity.setSkinState(ZbSkinState.LOADED);
                    ZbSkinCache.put(username, defaultSkin);
                });
            }
        });

        skinThread.setDaemon(true);
        skinThread.setName("ZbSkinLoader-" + username);
        skinThread.start();
    }

    /**
     * 获取默认皮肤
     */
    private static ResourceLocation getDefaultSkin(String username) {
        // 使用 Minecraft 的默认玩家皮肤机制
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
        return net.minecraft.client.resources.DefaultPlayerSkin.getDefaultTexture();
    }
}