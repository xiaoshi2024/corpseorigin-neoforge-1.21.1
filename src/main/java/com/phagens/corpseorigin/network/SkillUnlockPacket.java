package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.skill.ISkill;
import com.phagens.corpseorigin.skill.SkillManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 技能解锁请求包
 */
public record SkillUnlockPacket(ResourceLocation skillId) implements CustomPacketPayload {
    
    public static final Type<SkillUnlockPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("corpseorigin", "skill_unlock"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillUnlockPacket> STREAM_CODEC = 
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    SkillUnlockPacket::skillId,
                    SkillUnlockPacket::new
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    /**
     * 服务器端处理
     */
    public static void handleOnServer(SkillUnlockPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ISkill skill = SkillManager.getInstance().getSkill(packet.skillId);
                
                if (skill == null) {
                    player.sendSystemMessage(Component.translatable("message.corpseorigin.skill_not_found"));
                    return;
                }
                
                var handler = SkillManager.getInstance().getSkillHandler(player);
                
                // 检查是否已经学习
                if (handler.hasLearned(packet.skillId)) {
                    player.sendSystemMessage(Component.translatable("message.corpseorigin.skill_already_learned"));
                    return;
                }

                // 检查进化点数
                if (handler.getEvolutionPoints() < skill.getCost()) {
                    player.sendSystemMessage(Component.translatable("message.corpseorigin.not_enough_points"));
                    return;
                }

                // 检查前置技能
                for (ResourceLocation prereq : skill.getPrerequisites()) {
                    if (!handler.hasLearned(prereq)) {
                        player.sendSystemMessage(Component.translatable("message.corpseorigin.missing_prerequisite"));
                        return;
                    }
                }

                // 消耗进化点数并学习技能
                handler.consumeEvolutionPoints(skill.getCost());
                handler.learnSkill(skill);

                // 发送成功消息
                player.sendSystemMessage(Component.translatable("message.corpseorigin.skill_learned", skill.getName()));

                // 同步数据到客户端
                handler.syncToClient();
            }
        });
    }
}
