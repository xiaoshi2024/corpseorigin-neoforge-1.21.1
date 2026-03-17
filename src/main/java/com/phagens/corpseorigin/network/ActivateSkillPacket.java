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
 * 激活技能包 - 客户端到服务器
 */
public record ActivateSkillPacket(ResourceLocation skillId) implements CustomPacketPayload {
    
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(
            CorpseOrigin.MODID, "activate_skill");
    
    public static final Type<ActivateSkillPacket> TYPE = new Type<>(ID);
    
    public static final StreamCodec<ByteBuf, ActivateSkillPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            ActivateSkillPacket::skillId,
            ActivateSkillPacket::new
    );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 服务器端处理
     */
    public static void handleServer(ActivateSkillPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                ISkill skill = SkillManager.getInstance().getSkill(packet.skillId());
                
                if (skill == null) {
                    CorpseOrigin.LOGGER.warn("玩家 {} 尝试激活未知技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                    return;
                }
                
                ISkillHandler handler = SkillAttachment.getSkillHandler(serverPlayer);
                
                // 检查是否已学习此技能
                if (!handler.hasLearned(skill)) {
                    CorpseOrigin.LOGGER.warn("玩家 {} 尝试激活未学习的技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                    return;
                }
                
                // 检查是否是可激活技能
                if (!skill.isActivatable()) {
                    CorpseOrigin.LOGGER.warn("玩家 {} 尝试激活被动技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                    return;
                }
                
                // 检查冷却
                if (handler.isOnCooldown(skill)) {
                    CorpseOrigin.LOGGER.debug("玩家 {} 尝试激活冷却中的技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                    return;
                }
                
                // 激活技能
                boolean success = handler.activateSkill(skill);
                
                if (success) {
                    CorpseOrigin.LOGGER.debug("玩家 {} 成功激活技能 {}", 
                            serverPlayer.getName().getString(), packet.skillId());
                } else {
                    CorpseOrigin.LOGGER.debug("玩家 {} 激活技能 {} 失败", 
                            serverPlayer.getName().getString(), packet.skillId());
                }
            }
        });
    }
}
