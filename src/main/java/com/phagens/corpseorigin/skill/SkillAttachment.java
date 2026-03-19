package com.phagens.corpseorigin.skill;

import com.mojang.serialization.Codec;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 技能数据附件 - 将技能处理器绑定到玩家
 */
public class SkillAttachment {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CorpseOrigin.MODID);

    /**
     * 技能处理器附件 - 存储玩家的技能数据
     */
    public static final Supplier<AttachmentType<SkillHandler>> SKILL_HANDLER = ATTACHMENT_TYPES.register(
            "skill_handler",
            () -> AttachmentType.<SkillHandler>builder(() -> new SkillHandler(null))
                    .serialize(SkillHandler.CODEC)
                    .copyOnDeath()
                    .build()
    );

    /**
     * 获取玩家的技能处理器
     */
    public static ISkillHandler getSkillHandler(Player player) {
        if (player == null) return null;

        SkillHandler handler = player.getData(SKILL_HANDLER);
        if (handler == null) {
            handler = new SkillHandler(player);
            player.setData(SKILL_HANDLER, handler);
            CorpseOrigin.LOGGER.info("【SkillAttachment】为玩家 {} 创建新的技能处理器", player.getName().getString());
        } else {
            // 检查处理器中的数据
            CorpseOrigin.LOGGER.info("【SkillAttachment】玩家 {} 已有技能处理器: {} 个技能, {} 进化点",
                    player.getName().getString(),
                    handler.getLearnedSkillIds().size(),
                    handler.getEvolutionPoints());

            if (handler.getPlayer() == null) {
                handler.setPlayer(player);
                CorpseOrigin.LOGGER.info("【SkillAttachment】重新设置玩家引用");
            }

            // 如果是服务端，强制同步到客户端
            if (!player.level().isClientSide) {
                handler.markDirty();
                handler.syncToClient();
                CorpseOrigin.LOGGER.info("【SkillAttachment】强制同步技能数据到客户端");
            }
        }
        return handler;
    }

    /**
     * 设置玩家的技能处理器
     */
    public static void setSkillHandler(Player player, SkillHandler handler) {
        player.setData(SKILL_HANDLER, handler);
    }
}