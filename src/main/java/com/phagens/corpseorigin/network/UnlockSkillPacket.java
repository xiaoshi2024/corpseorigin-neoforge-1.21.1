package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.skill.ISkill;
import com.phagens.corpseorigin.skill.ISkillHandler;
import com.phagens.corpseorigin.skill.SkillAttachment;
import com.phagens.corpseorigin.skill.SkillManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 解锁技能包 - 客户端到服务器
 */
public record UnlockSkillPacket(ResourceLocation skillId) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            CorpseOrigin.MODID, "unlock_skill");
    
    public static final Type<UnlockSkillPacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<ByteBuf, UnlockSkillPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            UnlockSkillPacket::skillId,
            UnlockSkillPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 服务器端处理
     */
    public static void handleServer(UnlockSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ISkill skill = SkillManager.getInstance().getSkill(packet.skillId());
                
                if (skill == null) {
                    CorpseOrigin.LOGGER.warn("玩家 {} 尝试解锁未知技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                    return;
                }
                
                ISkillHandler handler = SkillAttachment.getSkillHandler(serverPlayer);
                ISkillHandler.LearnResult result = handler.learnSkill(skill);
                
                if (result.isSuccess()) {
                    CorpseOrigin.LOGGER.debug("玩家 {} 成功解锁技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                } else {
                    CorpseOrigin.LOGGER.debug("玩家 {} 解锁技能 {} 失败: {}", 
                            serverPlayer.getName().getString(), packet.skillId(), result.getMessage());
                }
            }
        });
    }
}
