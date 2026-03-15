package com.phagens.corpseorigin.client.skin;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 皮肤缓存系统
 * 避免重复加载同一玩家的皮肤
 */
public class ZbSkinCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final long CACHE_EXPIRE_TIME = 30 * 60 * 1000; // 30分钟过期

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final ResourceLocation texture;
        final long timestamp;

        CacheEntry(ResourceLocation texture) {
            this.texture = texture;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_EXPIRE_TIME;
        }
    }

    /**
     * 获取缓存的皮肤
     */
    public static ResourceLocation get(String username) {
        CacheEntry entry = CACHE.get(username);
        if (entry != null && !entry.isExpired()) {
            LOGGER.debug("✅ 使用缓存皮肤: {}", username);
            return entry.texture;
        }
        if (entry != null) {
            // 过期移除
            CACHE.remove(username);
            LOGGER.debug("🗑️ 皮肤缓存过期: {}", username);
        }
        return null;
    }

    /**
     * 缓存皮肤
     */
    public static void put(String username, ResourceLocation texture) {
        if (username != null && texture != null) {
            CACHE.put(username, new CacheEntry(texture));
            LOGGER.debug("💾 皮肤已缓存: {}", username);
        }
    }

    /**
     * 清除指定用户的缓存
     */
    public static void clear(String username) {
        CACHE.remove(username);
        LOGGER.debug("🧹 清除皮肤缓存: {}", username);
    }

    /**
     * 清除所有缓存
     */
    public static void clearAll() {
        CACHE.clear();
        LOGGER.info("🧹 已清除所有皮肤缓存");
    }

    /**
     * 获取缓存大小
     */
    public static int getCacheSize() {
        return CACHE.size();
    }
}