package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.ZbSkinUpdatePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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

        CorpseOrigin.LOGGER.info("网络包注册完成");
    }
}