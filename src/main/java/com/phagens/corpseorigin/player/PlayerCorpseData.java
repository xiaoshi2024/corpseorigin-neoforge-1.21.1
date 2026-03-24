package com.phagens.corpseorigin.player;

import com.phagens.corpseorigin.network.PlayerCorpseSyncPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerCorpseData {
    private static final String KEY_ORIGINAL_NAME = "original_name";
    private static final String KEY_SKIN_UUID = "skin_uuid";
    private static final String KEY_EVOLUTION_LEVEL = "evolution_level";
    private static final String KEY_KILLS = "kills";
    private static final String KEY_HUNGER = "hunger";
    private static final String KEY_HAS_SENTIENT = "has_sentient";
    private static final String KEY_IS_GREEDY = "is_greedy";
    private static final String KEY_VARIANT = "variant";
    private static final String KEY_HAS_WING = "has_wing";
    private static final String KEY_HAS_TAIL = "has_tail";
    private static final String KEY_IS_DISGUISED = "is_disguised";

    public static void setPlayerAsCorpse(Player player, int corpseType) {
        player.setData(CorpsePlayerAttachment.IS_CORPSE, true);
        player.setData(CorpsePlayerAttachment.CORPSE_TYPE, corpseType);

        CompoundTag data = new CompoundTag();
        data.putString(KEY_ORIGINAL_NAME, player.getName().getString());
        data.putString(KEY_SKIN_UUID, player.getUUID().toString());
        data.putInt(KEY_EVOLUTION_LEVEL, 1);
        data.putInt(KEY_KILLS, 0);
        data.putInt(KEY_HUNGER, 100);
        data.putBoolean(KEY_HAS_SENTIENT, player.getRandom().nextFloat() < 0.3f);
        data.putBoolean(KEY_IS_GREEDY, player.getRandom().nextFloat() < 0.5f);
        data.putInt(KEY_VARIANT, player.getRandom().nextFloat() < 0.3f ? 1 : 0);
        data.putBoolean(KEY_HAS_WING, false);
        data.putBoolean(KEY_HAS_TAIL, false);
        data.putBoolean(KEY_IS_DISGUISED, false);

        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static void removeCorpseState(Player player) {
        player.setData(CorpsePlayerAttachment.IS_CORPSE, false);
        player.setData(CorpsePlayerAttachment.CORPSE_TYPE, 0);
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, new CompoundTag());
        syncToClient(player);
    }

    public static boolean isCorpse(Player player) {
        return player.getData(CorpsePlayerAttachment.IS_CORPSE);
    }

    public static int getCorpseType(Player player) {
        return player.getData(CorpsePlayerAttachment.CORPSE_TYPE);
    }

    public static CompoundTag getCorpseData(Player player) {
        return player.getData(CorpsePlayerAttachment.CORPSE_DATA);
    }

    public static String getOriginalName(Player player) {
        return getCorpseData(player).getString(KEY_ORIGINAL_NAME);
    }

    public static String getSkinUuid(Player player) {
        return getCorpseData(player).getString(KEY_SKIN_UUID);
    }

    public static int getEvolutionLevel(Player player) {
        return getCorpseData(player).getInt(KEY_EVOLUTION_LEVEL);
    }

    public static void setEvolutionLevel(Player player, int level) {
        CompoundTag data = getCorpseData(player);
        data.putInt(KEY_EVOLUTION_LEVEL, Math.max(1, Math.min(5, level)));
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static int getKills(Player player) {
        return getCorpseData(player).getInt(KEY_KILLS);
    }

    public static void addKill(Player player) {
        CompoundTag data = getCorpseData(player);
        data.putInt(KEY_KILLS, data.getInt(KEY_KILLS) + 1);
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static int getHunger(Player player) {
        return getCorpseData(player).getInt(KEY_HUNGER);
    }

    public static void setHunger(Player player, int hunger) {
        CompoundTag data = getCorpseData(player);
        data.putInt(KEY_HUNGER, Math.max(0, Math.min(100, hunger)));
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static boolean hasSentient(Player player) {
        return getCorpseData(player).getBoolean(KEY_HAS_SENTIENT);
    }

    public static boolean isGreedy(Player player) {
        return getCorpseData(player).getBoolean(KEY_IS_GREEDY);
    }

    public static int getVariant(Player player) {
        return getCorpseData(player).getInt(KEY_VARIANT);
    }

    public static boolean hasWing(Player player) {
        return getCorpseData(player).getBoolean(KEY_HAS_WING);
    }

    public static void setHasWing(Player player, boolean hasWing) {
        CompoundTag data = getCorpseData(player);
        data.putBoolean(KEY_HAS_WING, hasWing);
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static boolean hasTail(Player player) {
        return getCorpseData(player).getBoolean(KEY_HAS_TAIL);
    }

    public static void setHasTail(Player player, boolean hasTail) {
        CompoundTag data = getCorpseData(player);
        data.putBoolean(KEY_HAS_TAIL, hasTail);
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    public static boolean isDisguised(Player player) {
        return getCorpseData(player).getBoolean(KEY_IS_DISGUISED);
    }

    public static void setDisguised(Player player, boolean isDisguised) {
        CompoundTag data = getCorpseData(player);
        data.putBoolean(KEY_IS_DISGUISED, isDisguised);
        player.setData(CorpsePlayerAttachment.CORPSE_DATA, data);
        syncToClient(player);
    }

    private static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                    serverPlayer.getId(),
                    isCorpse(serverPlayer),
                    getCorpseType(serverPlayer),
                    getCorpseData(serverPlayer).copy()
            );
            PacketDistributor.sendToPlayer(serverPlayer, packet);
        }
    }
}