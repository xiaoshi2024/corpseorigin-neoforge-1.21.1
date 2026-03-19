package com.phagens.corpseorigin.network;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import com.phagens.corpseorigin.skill.SkillAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ExperienceConvertPacket() implements CustomPacketPayload {
    
    public static final Type<ExperienceConvertPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("corpseorigin", "experience_convert"));
    
    public static final StreamCodec<RegistryFriendlyByteBuf, ExperienceConvertPacket> STREAM_CODEC = 
            StreamCodec.of(
                    (buffer, packet) -> {},
                    buffer -> new ExperienceConvertPacket()
            );
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
    public static void handleOnServer(ExperienceConvertPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // 检查玩家是否是尸族
                if (PlayerCorpseData.isCorpse(player)) {
                    // 检查玩家是否有足够的经验等级
                    if (player.experienceLevel >= 5) {
                        // 每次只转化5级经验，获得1点进化点
                        int remainingLevels = player.experienceLevel - 5;
                        
                        // 重置经验
                        player.totalExperience = 0;
                        player.experienceLevel = remainingLevels;
                        player.experienceProgress = 0.0f;
                        
                        // 增加进化点
                        var skillHandler = SkillAttachment.getSkillHandler(player);
                        if (skillHandler != null) {
                            skillHandler.addEvolutionPoints(1);
                            player.sendSystemMessage(net.minecraft.network.chat.Component.translatable("message.corpseorigin.experience_converted"));
                            CorpseOrigin.LOGGER.info("尸族玩家 {} 的 5 级经验已转化为 1 点进化点", player.getName().getString());
                        }
                    } else {
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c需要至少5级经验才能转化！"));
                    }
                } else {
                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§c只有尸族玩家才能转化经验！"));
                }
            }
        });
    }
}