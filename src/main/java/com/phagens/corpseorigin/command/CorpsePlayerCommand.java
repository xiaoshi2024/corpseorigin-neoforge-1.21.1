package com.phagens.corpseorigin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.PlayerCorpseSyncPacket;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import com.phagens.corpseorigin.skill.CorpseSkills;
import com.phagens.corpseorigin.skill.ISkillHandler;
import com.phagens.corpseorigin.skill.SkillAttachment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class CorpsePlayerCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("corpseplayer")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("type", IntegerArgumentType.integer(0, 5))
                            .executes(context -> {
                                ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                int type = IntegerArgumentType.getInteger(context, "type");
                                
                                PlayerCorpseData.setPlayerAsCorpse(player, type);
                                
                                // 初始化技能系统
                                ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);
                                
                                // 自动解锁初始技能（硬化皮肤）
                                if (!skillHandler.hasLearned(CorpseSkills.HARDENED_SKIN.getId())) {
                                    skillHandler.learnSkill(CorpseSkills.HARDENED_SKIN);
                                }
                                
                                // 给予一些初始进化点
                                skillHandler.addEvolutionPoints(5);
                                skillHandler.syncToClient();
                                
                                // 同步到客户端
                                PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                    player.getId(), true, type, PlayerCorpseData.getCorpseData(player)
                                );
                                PacketDistributor.sendToPlayer(player, packet);
                                PacketDistributor.sendToPlayersTrackingEntity(player, packet);
                                
                                // 发送提示给玩家
                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                    "§c§l你已成为尸兄！§r\n" +
                                    "§7按 §eK§7 打开技能树，按 §eR§7 打开技能轮盘\n" +
                                    "§7击杀生物可获得进化点来解锁更多技能！"
                                ));
                                
                                context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal(
                                        "已将玩家 " + player.getName().getString() + " 设置为尸兄状态，类型: " + type + "，获得5点初始进化点"
                                    ), true
                                );
                                return 1;
                            })
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                            
                            PlayerCorpseData.removeCorpseState(player);
                            
                            // 同步到客户端
                            PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                player.getId(), false, 0, PlayerCorpseData.getCorpseData(player)
                            );
                            PacketDistributor.sendToPlayer(player, packet);
                            PacketDistributor.sendToPlayersTrackingEntity(player, packet);
                            
                            context.getSource().sendSuccess(
                                () -> net.minecraft.network.chat.Component.literal(
                                    "已移除玩家 " + player.getName().getString() + " 的尸兄状态"
                                ), true
                            );
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("info")
                    .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                            boolean isCorpse = PlayerCorpseData.isCorpse(player);
                            int type = PlayerCorpseData.getCorpseType(player);
                            int hunger = PlayerCorpseData.getHunger(player);
                            int evolution = PlayerCorpseData.getEvolutionLevel(player);
                            int kills = PlayerCorpseData.getKills(player);
                            boolean hasSentient = PlayerCorpseData.hasSentient(player);
                            boolean isGreedy = PlayerCorpseData.isGreedy(player);
                            int variant = PlayerCorpseData.getVariant(player);

                            // 获取技能系统进化点
                            var skillHandler = SkillAttachment.getSkillHandler(player);
                            int skillPoints = skillHandler.getEvolutionPoints();

                            context.getSource().sendSuccess(
                                () -> net.minecraft.network.chat.Component.literal(
                                    "玩家 " + player.getName().getString() + " 状态:\n" +
                                    "  是否为尸兄: " + isCorpse + "\n" +
                                    "  尸兄类型: " + type + "\n" +
                                    "  饥饿度: " + hunger + "\n" +
                                    "  进化等级: " + evolution + "\n" +
                                    "  击杀数: " + kills + "\n" +
                                    "  保留神志: " + hasSentient + "\n" +
                                    "  贪婪: " + isGreedy + "\n" +
                                    "  变种: " + (variant == 1 ? "裂口" : "普通") + "\n" +
                                    "  进化点数(技能): " + skillPoints
                                ), false
                            );
                            return 1;
                        })
                    )
                )
                .then(Commands.literal("points")
                    .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("add")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    var skillHandler = SkillAttachment.getSkillHandler(player);
                                    skillHandler.addEvolutionPoints(amount);
                                    skillHandler.syncToClient();

                                    context.getSource().sendSuccess(
                                        () -> net.minecraft.network.chat.Component.literal(
                                            "已为玩家 " + player.getName().getString() + " 增加 " + amount + " 进化点"
                                        ), true
                                    );
                                    return 1;
                                })
                            )
                        )
                        .then(Commands.literal("set")
                            .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                    var skillHandler = SkillAttachment.getSkillHandler(player);
                                    skillHandler.setEvolutionPoints(amount);
                                    skillHandler.syncToClient();

                                    context.getSource().sendSuccess(
                                        () -> net.minecraft.network.chat.Component.literal(
                                            "已将玩家 " + player.getName().getString() + " 的进化点设置为 " + amount
                                        ), true
                                    );
                                    return 1;
                                })
                            )
                        )
                    )
                )
        );
    }
}
