package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.CorpseOrigin;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 网络注册 - 只注册非技能相关的网络包
 * 技能相关的包在 NetworkPaketGL 中统一注册
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class NetworkRegister {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(CorpseOrigin.MODID)
                .versioned("1.0.0");

        // 注册皮肤更新包 (客户端 → 服务端)
        registrar.playToServer(
                ZbSkinUpdatePacket.TYPE,
                ZbSkinUpdatePacket.STREAM_CODEC,
                ZbSkinUpdatePacket::handleData
        );

        // 注册玩家尸体状态同步包 (服务端 → 客户端)
        registrar.playToClient(
                PlayerCorpseSyncPacket.TYPE,
                PlayerCorpseSyncPacket.STREAM_CODEC,
                PlayerCorpseSyncPacket::handle
        );

        // 注意：所有技能相关的包（SyncSkillDataPacket, ActivateSkillPacket, SkillUnlockPacket等）
        // 都在 NetworkPaketGL 中统一注册，避免重复注册错误

        CorpseOrigin.LOGGER.info("NetworkRegister 网络包注册完成");
    }
}