package com.phagens.corpseorigin.data;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

/**
 * 尸王之力数据存储 - 持久化感染和控制信息
 */
public class CorpseKingData extends SavedData {
    private static final String DATA_NAME = CorpseOrigin.MODID + "_corpse_king_data";

    // 玩家控制信息存储：被控制者UUID -> 控制信息
    private final Map<UUID, PlayerControlEntry> playerControls = new HashMap<>();
    // 尸王的手下：尸王UUID -> 手下UUID集合
    private final Map<UUID, Set<UUID>> zombieMinions = new HashMap<>();

    public CorpseKingData() {
    }

    public static CorpseKingData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(CorpseKingData::new, CorpseKingData::load),
            DATA_NAME
        );
    }

    public static CorpseKingData load(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        CorpseKingData data = new CorpseKingData();

        // 加载玩家控制数据
        if (tag.contains("player_controls")) {
            ListTag controlList = tag.getList("player_controls", 10);
            for (int i = 0; i < controlList.size(); i++) {
                CompoundTag controlTag = controlList.getCompound(i);
                UUID controlledUUID = controlTag.getUUID("controlled_uuid");
                UUID controllerUUID = controlTag.getUUID("controller_uuid");
                long controlStartTime = controlTag.getLong("control_start_time");
                int duration = controlTag.getInt("duration");
                boolean frozen = controlTag.getBoolean("frozen");

                data.playerControls.put(controlledUUID, new PlayerControlEntry(
                    controllerUUID, controlStartTime, duration, frozen
                ));
            }
        }

        // 加载手下数据
        if (tag.contains("zombie_minions")) {
            ListTag minionList = tag.getList("zombie_minions", 10);
            for (int i = 0; i < minionList.size(); i++) {
                CompoundTag minionTag = minionList.getCompound(i);
                UUID masterUUID = minionTag.getUUID("master_uuid");
                ListTag minionsTag = minionTag.getList("minions", 8); // 8 = String
                Set<UUID> minions = new HashSet<>();
                for (int j = 0; j < minionsTag.size(); j++) {
                    String uuidStr = minionsTag.getString(j);
                    try {
                        minions.add(UUID.fromString(uuidStr));
                    } catch (IllegalArgumentException e) {
                        // 忽略无效的UUID
                    }
                }
                data.zombieMinions.put(masterUUID, minions);
            }
        }

        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        // 保存玩家控制数据
        ListTag controlList = new ListTag();
        for (Map.Entry<UUID, PlayerControlEntry> entry : playerControls.entrySet()) {
            // 跳过已过期的控制
            if (entry.getValue().isExpired()) continue;

            CompoundTag controlTag = new CompoundTag();
            controlTag.putUUID("controlled_uuid", entry.getKey());
            controlTag.putUUID("controller_uuid", entry.getValue().controllerUUID);
            controlTag.putLong("control_start_time", entry.getValue().controlStartTime);
            controlTag.putInt("duration", entry.getValue().durationTicks);
            controlTag.putBoolean("frozen", entry.getValue().frozen);
            controlList.add(controlTag);
        }
        tag.put("player_controls", controlList);

        // 保存手下数据
        ListTag minionList = new ListTag();
        for (Map.Entry<UUID, Set<UUID>> entry : zombieMinions.entrySet()) {
            CompoundTag minionTag = new CompoundTag();
            minionTag.putUUID("master_uuid", entry.getKey());
            ListTag minionsTag = new ListTag();
            for (UUID minionUUID : entry.getValue()) {
                minionsTag.add(net.minecraft.nbt.StringTag.valueOf(minionUUID.toString()));
            }
            minionTag.put("minions", minionsTag);
            minionList.add(minionTag);
        }
        tag.put("zombie_minions", minionList);

        return tag;
    }

    // ========== 玩家控制相关方法 ==========

    public void addPlayerControl(UUID controlledUUID, UUID controllerUUID, int durationTicks) {
        playerControls.put(controlledUUID, new PlayerControlEntry(
            controllerUUID, System.currentTimeMillis(), durationTicks, true
        ));
        setDirty();
    }

    public void removePlayerControl(UUID controlledUUID) {
        playerControls.remove(controlledUUID);
        setDirty();
    }

    public PlayerControlEntry getPlayerControl(UUID controlledUUID) {
        PlayerControlEntry entry = playerControls.get(controlledUUID);
        if (entry != null && entry.isExpired()) {
            playerControls.remove(controlledUUID);
            return null;
        }
        return entry;
    }

    public Map<UUID, PlayerControlEntry> getAllPlayerControls() {
        // 清理过期的
        playerControls.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return new HashMap<>(playerControls);
    }

    // ========== 手下相关方法 ==========

    public void addMinion(UUID masterUUID, UUID minionUUID) {
        zombieMinions.computeIfAbsent(masterUUID, k -> new HashSet<>()).add(minionUUID);
        setDirty();
    }

    public void removeMinion(UUID masterUUID, UUID minionUUID) {
        Set<UUID> minions = zombieMinions.get(masterUUID);
        if (minions != null) {
            minions.remove(minionUUID);
            if (minions.isEmpty()) {
                zombieMinions.remove(masterUUID);
            }
            setDirty();
        }
    }

    public Set<UUID> getMinions(UUID masterUUID) {
        return new HashSet<>(zombieMinions.getOrDefault(masterUUID, Collections.emptySet()));
    }

    public Map<UUID, Set<UUID>> getAllMinions() {
        return new HashMap<>(zombieMinions);
    }

    public void clearMinions(UUID masterUUID) {
        zombieMinions.remove(masterUUID);
        setDirty();
    }

    // ========== 清理方法 ==========

    public void cleanupExpired() {
        boolean changed = playerControls.entrySet().removeIf(entry -> entry.getValue().isExpired());
        if (changed) {
            setDirty();
        }
    }

    // ========== 数据类 ==========

    public static class PlayerControlEntry {
        public final UUID controllerUUID;
        public final long controlStartTime;
        public final int durationTicks;
        public final boolean frozen;

        public PlayerControlEntry(UUID controllerUUID, long controlStartTime, int durationTicks, boolean frozen) {
            this.controllerUUID = controllerUUID;
            this.controlStartTime = controlStartTime;
            this.durationTicks = durationTicks;
            this.frozen = frozen;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - controlStartTime > (durationTicks * 50L);
        }
    }
}
