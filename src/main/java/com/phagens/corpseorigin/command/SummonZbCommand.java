package com.phagens.corpseorigin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class SummonZbCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("summonzb")
                .requires(source -> source.hasPermission(2)) // 需要OP权限
                // 无参数版本 - 随机皮肤
                .executes(context -> summonZbRandom(context))
                // 带玩家名参数版本
                .then(Commands.argument("playerName", StringArgumentType.greedyString())
                    .executes(context -> {
                        String playerName = StringArgumentType.getString(context, "playerName");
                        return summonZbByName(context, playerName);
                    })
                )
        );
    }

    /**
     * 召唤随机皮肤的尸兄
     */
    private static int summonZbRandom(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        // 创建尸兄实体（无皮肤）
        LowerLevelZbEntity zb = new LowerLevelZbEntity(
            EntityRegistry.LOWER_LEVEL_ZB.get(),
            level
        );

        // 设置位置
        zb.setPos(pos.x, pos.y, pos.z);

        // 添加到世界
        level.addFreshEntity(zb);

        // 发送反馈消息
        source.sendSuccess(() -> 
            Component.literal("§a成功召唤尸兄！§7(随机皮肤)"), 
            true
        );
        CorpseOrigin.LOGGER.info("管理员 {} 召唤了尸兄（随机皮肤）", source.getTextName());

        return 1;
    }

    /**
     * 通过玩家名称召唤尸兄
     * 支持任意玩家名，通过万用皮肤补丁加载皮肤
     */
    private static int summonZbByName(CommandContext<CommandSourceStack> context, String playerName) {
        CommandSourceStack source = context.getSource();
        ServerLevel level = source.getLevel();
        Vec3 pos = source.getPosition();

        // 创建尸兄实体
        LowerLevelZbEntity zb = new LowerLevelZbEntity(
            EntityRegistry.LOWER_LEVEL_ZB.get(),
            level
        );

        // 设置皮肤名称（万用皮肤补丁会自动从皮肤站加载）
        zb.setPlayerSkinName(playerName);
        
        // 设置自定义 ID
        zb.setCustomId(playerName);
        
        source.sendSuccess(() -> 
            Component.literal("§a成功召唤尸兄！皮肤玩家: §e" + playerName), 
            true
        );
        CorpseOrigin.LOGGER.info("管理员 {} 召唤了尸兄，使用玩家 {} 的皮肤", 
            source.getTextName(), playerName);

        // 设置位置
        zb.setPos(pos.x, pos.y, pos.z);

        // 添加到世界
        level.addFreshEntity(zb);

        return 1;
    }
}
