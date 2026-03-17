package com.phagens.corpseorigin.skill;

import com.mojang.serialization.Codec;
import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
            () -> AttachmentType.builder(() -> new SkillHandler(null))
                    .serialize(new SkillHandlerCodec())
                    .copyOnDeath()
                    .build()
    );
    
    /**
     * 获取玩家的技能处理器
     */
    public static ISkillHandler getSkillHandler(Player player) {
        SkillHandler handler = player.getData(SKILL_HANDLER);
        if (handler == null || handler.getPlayer() == null) {
            handler = new SkillHandler(player);
            player.setData(SKILL_HANDLER, handler);
        }
        return handler;
    }
    
    /**
     * 设置玩家的技能处理器
     */
    public static void setSkillHandler(Player player, SkillHandler handler) {
        player.setData(SKILL_HANDLER, handler);
    }
    
    /**
     * 技能处理器编解码器
     */
    private static class SkillHandlerCodec implements Codec<SkillHandler> {
        @Override
        public <T> com.mojang.serialization.DataResult<T> encode(SkillHandler input, 
                com.mojang.serialization.DynamicOps<T> ops, T prefix) {
            CompoundTag tag = input.saveToNBT();
            return ops.mergeToPrimitive(prefix, ops.createString(tag.toString()));
        }
        
        @Override
        public <T> com.mojang.serialization.DataResult<com.mojang.datafixers.util.Pair<SkillHandler, T>> decode(
                com.mojang.serialization.DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).flatMap(str -> {
                try {
                    CompoundTag tag = net.minecraft.nbt.TagParser.parseTag(str);
                    // 创建一个临时的 SkillHandler，玩家引用会在加载时设置
                    SkillHandler handler = new SkillHandler(null);
                    handler.loadFromNBT(tag);
                    return com.mojang.serialization.DataResult.success(
                        com.mojang.datafixers.util.Pair.of(handler, input));
                } catch (Exception e) {
                    return com.mojang.serialization.DataResult.error(
                        () -> "Failed to parse SkillHandler: " + e.getMessage());
                }
            });
        }
    }
}
