package com.phagens.corpseorigin.command;

import com.mojang.brigadier.CommandDispatcher;
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

                        // 设置尸兄状态
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("type", IntegerArgumentType.integer(0, 5))
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    int type = IntegerArgumentType.getInteger(context, "type");

                                                    PlayerCorpseData.setPlayerAsCorpse(player, type);

                                                    ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);

                                                    if (!skillHandler.hasLearned(CorpseSkills.HARDENED_SKIN.getId())) {
                                                        skillHandler.learnSkill(CorpseSkills.HARDENED_SKIN);
                                                    }
                                                    if (!skillHandler.hasLearned(CorpseSkills.SHARP_CLAWS.getId())) {
                                                        skillHandler.learnSkill(CorpseSkills.SHARP_CLAWS);
                                                    }

                                                    skillHandler.addEvolutionPoints(10);
                                                    skillHandler.syncToClient();

                                                    PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                                            player.getId(), true, type, PlayerCorpseData.getCorpseData(player)
                                                    );
                                                    PacketDistributor.sendToPlayer(player, packet);
                                                    PacketDistributor.sendToPlayersTrackingEntity(player, packet);

                                                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                            "§c§l你已成为尸兄！§r\n" +
                                                                    "§7按 §eK§7 打开技能树，按 §eR§7 打开技能轮盘\n" +
                                                                    "§7击杀生物可获得进化点来解锁更多技能！\n" +
                                                                    "§a已自动学习硬化皮肤和利爪，并获得10进化点！"
                                                    ));

                                                    context.getSource().sendSuccess(
                                                            () -> net.minecraft.network.chat.Component.literal(
                                                                    "已将玩家 " + player.getName().getString() + " 设置为尸兄状态，类型: " + type
                                                            ), true
                                                    );
                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // 设置尸兄等级
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    int level = IntegerArgumentType.getInteger(context, "level");

                                                    if (!PlayerCorpseData.isCorpse(player)) {
                                                        context.getSource().sendFailure(
                                                                net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                        );
                                                        return 0;
                                                    }

                                                    PlayerCorpseData.setEvolutionLevel(player, level);

                                                    PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                                            player.getId(), true, PlayerCorpseData.getCorpseType(player),
                                                            PlayerCorpseData.getCorpseData(player)
                                                    );
                                                    PacketDistributor.sendToPlayer(player, packet);
                                                    PacketDistributor.sendToPlayersTrackingEntity(player, packet);

                                                    context.getSource().sendSuccess(
                                                            () -> net.minecraft.network.chat.Component.literal(
                                                                    "已将玩家 " + player.getName().getString() + " 的尸兄等级设置为 " + level
                                                            ), true
                                                    );

                                                    player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                            "§a你的尸兄等级已提升至 " + level + " 级！"
                                                    ));

                                                    return 1;
                                                })
                                        )
                                )
                        )

                        // 测试技能
                        .then(Commands.literal("testskills")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            if (!PlayerCorpseData.isCorpse(player)) {
                                                context.getSource().sendFailure(
                                                        net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                );
                                                return 0;
                                            }

                                            PlayerCorpseData.setEvolutionLevel(player, 5);
                                            ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);

                                            if (!skillHandler.hasLearned(CorpseSkills.SHARP_CLAWS.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.SHARP_CLAWS);
                                            }
                                            if (!skillHandler.hasLearned(CorpseSkills.GIANT_STRENGTH.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.GIANT_STRENGTH);
                                            }
                                            if (!skillHandler.hasLearned(CorpseSkills.BERSERK.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.BERSERK);
                                            }
                                            if (!skillHandler.hasLearned(CorpseSkills.VENOM.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.VENOM);
                                            }
                                            if (!skillHandler.hasLearned(CorpseSkills.FEAR_AURA.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.FEAR_AURA);
                                            }

                                            skillHandler.addEvolutionPoints(20);
                                            skillHandler.syncToClient();

                                            context.getSource().sendSuccess(
                                                    () -> net.minecraft.network.chat.Component.literal(
                                                            "已为玩家 " + player.getName().getString() + " 添加测试技能：\n" +
                                                                    "  - 巨力（被动）\n" +
                                                                    "  - 狂暴（可激活）\n" +
                                                                    "  - 毒液（被动）\n" +
                                                                    "  - 恐惧光环（可激活）"
                                                    ), true
                                            );

                                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                    "§a你获得了测试技能！按 R 键打开技能轮盘查看可激活技能！"
                                            ));

                                            return 1;
                                        })
                                )
                        )

                        // 单独添加技能
                        .then(Commands.literal("addskill")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("skill", net.minecraft.commands.arguments.ResourceLocationArgument.id())
                                                .executes(context -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                    net.minecraft.resources.ResourceLocation skillId =
                                                            net.minecraft.commands.arguments.ResourceLocationArgument.getId(context, "skill");

                                                    if (!PlayerCorpseData.isCorpse(player)) {
                                                        context.getSource().sendFailure(
                                                                net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                        );
                                                        return 0;
                                                    }

                                                    ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);

                                                    var result = skillHandler.learnSkill(skillId);

                                                    if (result.isSuccess()) {
                                                        skillHandler.syncToClient();
                                                        context.getSource().sendSuccess(
                                                                () -> net.minecraft.network.chat.Component.literal(
                                                                        "成功为玩家 " + player.getName().getString() + " 添加技能"
                                                                ), true
                                                        );
                                                    } else {
                                                        context.getSource().sendFailure(
                                                                net.minecraft.network.chat.Component.literal(
                                                                        "添加技能失败: " + result.getMessage()
                                                                )
                                                        );
                                                    }
                                                    return result.isSuccess() ? 1 : 0;
                                                })
                                        )
                                )
                        )

                        // 移除尸兄状态
                        .then(Commands.literal("remove")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            PlayerCorpseData.removeCorpseState(player);

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

                        // 查看信息
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

                                            var skillHandler = SkillAttachment.getSkillHandler(player);
                                            int skillPoints = skillHandler.getEvolutionPoints();
                                            int learnedSkills = skillHandler.getLearnedSkills().size();

                                            context.getSource().sendSuccess(
                                                    () -> net.minecraft.network.chat.Component.literal(
                                                            "§6===== 玩家 " + player.getName().getString() + " 状态 =====\n" +
                                                                    "§e是否为尸兄: §f" + isCorpse + "\n" +
                                                                    "§e尸兄类型: §f" + type + "\n" +
                                                                    "§e饥饿度: §f" + hunger + "\n" +
                                                                    "§e进化等级: §f" + evolution + "\n" +
                                                                    "§e击杀数: §f" + kills + "\n" +
                                                                    "§e保留神志: §f" + hasSentient + "\n" +
                                                                    "§e贪婪: §f" + isGreedy + "\n" +
                                                                    "§e变种: §f" + (variant == 1 ? "裂口" : "普通") + "\n" +
                                                                    "§e已学习技能: §f" + learnedSkills + " 个\n" +
                                                                    "§e技能进化点: §f" + skillPoints
                                                    ), false
                                            );

                                            if (learnedSkills > 0) {
                                                StringBuilder skills = new StringBuilder("§7已学习技能: ");
                                                for (var skill : skillHandler.getLearnedSkills()) {
                                                    skills.append("\n  §b- ").append(skill.getName().getString());
                                                }
                                                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(skills.toString()));
                                            }

                                            return 1;
                                        })
                                )
                        )

                        // 进化点操作
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