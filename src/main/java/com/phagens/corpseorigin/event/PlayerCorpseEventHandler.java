package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.PlayerCorpseSyncPacket;
import com.phagens.corpseorigin.player.CorpsePlayerAttachment;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class PlayerCorpseEventHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCorpseStateToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCorpseStateToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncCorpseStateToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        boolean isCorpse = original.getData(CorpsePlayerAttachment.IS_CORPSE);
        int corpseType = original.getData(CorpsePlayerAttachment.CORPSE_TYPE);
        CompoundTag corpseData = original.getData(CorpsePlayerAttachment.CORPSE_DATA);

        newPlayer.setData(CorpsePlayerAttachment.IS_CORPSE, isCorpse);
        newPlayer.setData(CorpsePlayerAttachment.CORPSE_TYPE, corpseType);
        newPlayer.setData(CorpsePlayerAttachment.CORPSE_DATA, corpseData.copy());

        if (newPlayer instanceof ServerPlayer serverPlayer) {
            syncCorpseStateToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player instanceof ServerPlayer serverPlayer) {
            if (PlayerCorpseData.isCorpse(player)) {
                updateCorpseBehavior(serverPlayer);
            }
        }
    }

    private static void syncCorpseStateToClient(ServerPlayer player) {
        boolean isCorpse = player.getData(CorpsePlayerAttachment.IS_CORPSE);
        int corpseType = player.getData(CorpsePlayerAttachment.CORPSE_TYPE);
        CompoundTag corpseData = player.getData(CorpsePlayerAttachment.CORPSE_DATA);

        PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                player.getId(), isCorpse, corpseType, corpseData
        );

        PacketDistributor.sendToPlayer(player, packet);
        PacketDistributor.sendToPlayersTrackingEntity(player, packet);
    }

    private static void updateCorpseBehavior(ServerPlayer player) {
        if (player.tickCount % 100 == 0) {
            int hunger = PlayerCorpseData.getHunger(player);
            if (hunger > 0) {
                PlayerCorpseData.setHunger(player, hunger - 1);
            }
        }

        if (player.tickCount % 200 == 0 && PlayerCorpseData.hasSentient(player)) {
            if (player.getRandom().nextFloat() < 0.3f) {
                String[] phrases = {
                    "救...救我...",
                    "好饿...",
                    "我...我怎么了...",
                    "不要...不要杀我...",
                    "肉...肉...",
                    "谁来...救救我...",
                    "好疼...好疼...",
                    "我不想死..."
                };
                String phrase = phrases[player.getRandom().nextInt(phrases.length)];
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(phrase));
            }
        }
    }
}
