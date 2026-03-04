package com.phagens.corpseorigin.GongFU.PackGongFu.Paket;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
//数据包
public class OpenGongFuMenuPacket implements CustomPacketPayload {
    //数据包ID
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "open_gongfu_menu");

    // 简化的编解码器
    public static final StreamCodec<FriendlyByteBuf, OpenGongFuMenuPacket> STREAM_CODEC =
            new StreamCodec<>() {

                @Override//从网络缓冲区读取数据
                public OpenGongFuMenuPacket decode(FriendlyByteBuf buf) {
                    return new OpenGongFuMenuPacket();
                }

                @Override//将数据包写入网络缓存发送
                public void encode(FriendlyByteBuf buf, OpenGongFuMenuPacket packet) {

                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
   // 用于注册和识别数据包类型的包装器
    public static final Type<OpenGongFuMenuPacket> TYPE = new Type<>(ID);
}

