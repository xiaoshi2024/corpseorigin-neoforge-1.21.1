package com.phagens.corpseorigin.event.player;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.YaoJi.Sagent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.phagens.corpseorigin.GongFU.ModUtlis.GongFUDataUtlis.applyGongFaAttributes;

//兼容修行
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class playerDie {
    // 服务器级别的内存缓存
    private static final Map<UUID, CompoundTag> DEATH_BACKUP = new ConcurrentHashMap<>();
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {

            CompoundTag playerData = player.getPersistentData();
            CompoundTag gongFuData = playerData.getCompound("GongFuContainer");
            if (!gongFuData.isEmpty()) {
                // 保存到服务器内存缓存
                DEATH_BACKUP.put(player.getUUID(), gongFuData.copy());
            }

            // 玩家死亡时移除所有CorpseOrigin添加的属性修饰符
            Sagent.removeAllPlayerAttributes(player);
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){
        Player original = event.getOriginal();  // 死亡前的玩家实体
        Player newPlayer = event.getEntity();   // 重生后的玩家实体
        // 玩家重生时恢复数据
        UUID playerUUID = original.getUUID();
        if (DEATH_BACKUP.containsKey(playerUUID)) {
            CompoundTag backupData = DEATH_BACKUP.get(playerUUID);
            newPlayer.getPersistentData().put("GongFuContainer", backupData);
            DEATH_BACKUP.remove(playerUUID); // 清理缓存

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            applyGongFaAttributes(player);
        }
    }



}
