package com.phagens.corpseorigin.GongFU.PackGongFu;

import com.phagens.corpseorigin.GongFU.PackGongFu.Paket.OpenGongFuMenuPacket;
import com.phagens.corpseorigin.GongFU.Sceen.GongFuMenu;
import com.phagens.corpseorigin.network.ActivateSkillPacket;
import com.phagens.corpseorigin.network.SkillUnlockPacket;
import com.phagens.corpseorigin.network.SyncSkillDataPacket;
import com.phagens.corpseorigin.network.UnlockSkillPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//网络管理
public class NetworkPaketGL {

    // 公共静态注册方法
    public static void registerPackets(IEventBus modEventBus) {
        modEventBus.addListener(NetworkPaketGL::registerNetworkPackets);
    }
    public static void registerNetworkPackets(RegisterPayloadHandlersEvent event) {
        // 获取协议注册器 - 使用与 NetworkRegister 不同的版本号
        PayloadRegistrar registrar = event.registrar("corpseorigin_gongfu")
                .versioned("1.0.0");

        // 注册双向包（客户端发送，服务端接收）
        registrar.playBidirectional(
                OpenGongFuMenuPacket.TYPE,
                OpenGongFuMenuPacket.STREAM_CODEC,
                (packet, context) -> {
                    if (!context.flow().isClientbound()) {
                        // 服务端处理
                        handleOpenGongFuMenu(packet, context);
                    }
                    // 客户端不处理这个包
                }
        );

        // 注册技能同步包（服务器到客户端）
        registrar.playToClient(
                SyncSkillDataPacket.TYPE,
                SyncSkillDataPacket.STREAM_CODEC,
                SyncSkillDataPacket::handleClient
        );

        // 注册解锁技能包（客户端到服务器）- 使用旧的包
        registrar.playToServer(
                UnlockSkillPacket.TYPE,
                UnlockSkillPacket.STREAM_CODEC,
                UnlockSkillPacket::handleServer
        );

        // 注册激活技能包（客户端到服务器）
        registrar.playToServer(
                ActivateSkillPacket.TYPE,
                ActivateSkillPacket.STREAM_CODEC,
                ActivateSkillPacket::handleServer
        );

        // 注册新的技能解锁包
        registrar.playToServer(
                SkillUnlockPacket.TYPE,
                SkillUnlockPacket.STREAM_CODEC,
                SkillUnlockPacket::handleOnServer
        );
    }

    private static void handleOpenGongFuMenu(OpenGongFuMenuPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (containerId, inventory, player) -> new GongFuMenu(containerId, inventory),
                        Component.translatable("修行")
                ));
            }
        });

    }
}



