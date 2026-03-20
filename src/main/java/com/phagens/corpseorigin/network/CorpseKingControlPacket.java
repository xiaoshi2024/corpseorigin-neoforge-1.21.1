package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.CorpseOrigin;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 尸王控制同步包 - 用于同步控制者的输入到被控制玩家
 */
public record CorpseKingControlPacket(
        int controllerId,
        float lookX, float lookY, float lookZ,
        byte actionFlags
) implements CustomPacketPayload {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            CorpseOrigin.MODID, "corpse_king_control");

    public static final Type<CorpseKingControlPacket> TYPE = new Type<>(ID);

    // 动作标志位
    public static final byte FLAG_JUMPING = 0x01;
    public static final byte FLAG_SNEAKING = 0x02;
    public static final byte FLAG_ATTACKING = 0x04;

    public static final StreamCodec<ByteBuf, CorpseKingControlPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            CorpseKingControlPacket::controllerId,
            ByteBufCodecs.FLOAT,
            CorpseKingControlPacket::lookX,
            ByteBufCodecs.FLOAT,
            CorpseKingControlPacket::lookY,
            ByteBufCodecs.FLOAT,
            CorpseKingControlPacket::lookZ,
            ByteBufCodecs.BYTE,
            CorpseKingControlPacket::actionFlags,
            CorpseKingControlPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 创建数据包的便捷方法
     */
    public static CorpseKingControlPacket create(int controllerId, Vec3 look, boolean jumping, boolean sneaking, boolean attacking) {
        byte flags = 0;
        if (jumping) flags |= FLAG_JUMPING;
        if (sneaking) flags |= FLAG_SNEAKING;
        if (attacking) flags |= FLAG_ATTACKING;
        return new CorpseKingControlPacket(controllerId, (float) look.x, (float) look.y, (float) look.z, flags);
    }

    /**
     * 服务器端处理
     */
    public static void handleServer(CorpseKingControlPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer controller) {
                // 验证控制者身份并处理控制逻辑
                // 实际处理在技能类中完成
            }
        });
    }

    /**
     * 客户端处理
     */
    public static void handleClient(CorpseKingControlPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 客户端处理控制同步
        });
    }

    public Vec3 getLookVector() {
        return new Vec3(lookX, lookY, lookZ);
    }

    public boolean isJumping() {
        return (actionFlags & FLAG_JUMPING) != 0;
    }

    public boolean isSneaking() {
        return (actionFlags & FLAG_SNEAKING) != 0;
    }

    public boolean isAttacking() {
        return (actionFlags & FLAG_ATTACKING) != 0;
    }
}
