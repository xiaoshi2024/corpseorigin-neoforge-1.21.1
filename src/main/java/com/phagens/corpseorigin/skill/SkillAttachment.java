package com.phagens.corpseorigin.skill;

import com.mojang.serialization.Codec;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SkillAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CorpseOrigin.MODID);

    public static final Supplier<AttachmentType<SkillHandler>> SKILL_HANDLER = ATTACHMENT_TYPES.register(
            "skill_handler",
            () -> AttachmentType.<SkillHandler>builder(() -> new SkillHandler(null))
                    .serialize(SkillHandler.CODEC)
                    .copyOnDeath() // 这应该能工作，但我们还要加强
                    .build()
    );

    /**
     * 获取玩家的技能处理器 - 增强版本
     */
    /**
     * 获取玩家的技能处理器 - 修复客户端同步问题
     */
    public static ISkillHandler getSkillHandler(Player player) {
        if (player == null) return null;

        SkillHandler handler = player.getData(SKILL_HANDLER);

        if (handler == null) {
            CorpseOrigin.LOGGER.debug("为玩家 {} 创建新的技能处理器", player.getName().getString());
            handler = new SkillHandler(player);
            player.setData(SKILL_HANDLER, handler);
        } else if (handler.getPlayer() == null) {
            handler.setPlayer(player);

            // 客户端和服务端都打印日志
            if (player.level().isClientSide) {
                CorpseOrigin.LOGGER.info("【客户端】为玩家 {} 设置技能处理器，已学习 {} 个技能，进化点: {}",
                        player.getName().getString(),
                        handler.getLearnedSkills().size(),
                        handler.getEvolutionPoints());
            } else {
                CorpseOrigin.LOGGER.info("【服务端】为玩家 {} 设置技能处理器，已学习 {} 个技能，进化点: {}",
                        player.getName().getString(),
                        handler.getLearnedSkills().size(),
                        handler.getEvolutionPoints());

                // 只有在服务端才重新应用被动技能
                handler.reapplyPassiveSkills();
            }
        }

        return handler;
    }
}