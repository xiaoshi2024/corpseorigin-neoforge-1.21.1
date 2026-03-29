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
                                                    // 发送给所有在线玩家，确保所有人都能看到尸兄状态
                                                    PacketDistributor.sendToAllPlayers(packet);

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

                                                    // 检查是否达到3级，如果是且失去意识，则恢复意识
                                                    if (level >= 3 && !PlayerCorpseData.hasConsciousness(player)) {
                                                        PlayerCorpseData.restoreConsciousness(player);
                                                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                                "§a§l你的意识从混沌中苏醒！§r\n" +
                                                                "§7随着进化的提升，你重新获得了人类的智慧！"
                                                        ));
                                                    }

                                                    PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                                            player.getId(), true, PlayerCorpseData.getCorpseType(player),
                                                            PlayerCorpseData.getCorpseData(player)
                                                    );
                                                    // 发送给所有在线玩家
                                                    PacketDistributor.sendToAllPlayers(packet);

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
// 测试可激活技能 - 添加在 testskills 命令后面
                        .then(Commands.literal("testactivatable")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            if (!PlayerCorpseData.isCorpse(player)) {
                                                context.getSource().sendFailure(
                                                        net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                );
                                                return 0;
                                            }

                                            // 设置进化等级到5级
                                            PlayerCorpseData.setEvolutionLevel(player, 5);

                                            ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);

                                            // 先学习所有前置被动技能
                                            CorpseOrigin.LOGGER.info("===== 开始添加可激活技能测试 =====");

                                            // 1. 基础技能（被动）- 必需的前置
                                            if (!skillHandler.hasLearned(CorpseSkills.SHARP_CLAWS.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.SHARP_CLAWS);
                                                CorpseOrigin.LOGGER.info("添加基础技能: 利爪 (被动)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.HARDENED_SKIN.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.HARDENED_SKIN);
                                                CorpseOrigin.LOGGER.info("添加基础技能: 硬化皮肤 (被动)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.SWIFT_MOVEMENT.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.SWIFT_MOVEMENT);
                                                CorpseOrigin.LOGGER.info("添加基础技能: 疾行 (被动)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.REGENERATION.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.REGENERATION);
                                                CorpseOrigin.LOGGER.info("添加基础技能: 再生 (被动)");
                                            }

                                            // 2. 力量分支前置
                                            if (!skillHandler.hasLearned(CorpseSkills.GIANT_STRENGTH.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.GIANT_STRENGTH);
                                                CorpseOrigin.LOGGER.info("添加力量技能: 巨力 (被动)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.HEAVY_STRIKE.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.HEAVY_STRIKE);
                                                CorpseOrigin.LOGGER.info("添加力量技能: 重击 (被动)");
                                            }

                                            // 3. 敏捷分支前置
                                            if (!skillHandler.hasLearned(CorpseSkills.LEAP.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.LEAP);
                                                CorpseOrigin.LOGGER.info("添加敏捷技能: 跳跃 (被动)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.EVASION.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.EVASION);
                                                CorpseOrigin.LOGGER.info("添加敏捷技能: 闪避 (被动)");
                                            }

                                            // 4. 特殊分支前置
                                            if (!skillHandler.hasLearned(CorpseSkills.VENOM.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.VENOM);
                                                CorpseOrigin.LOGGER.info("添加特殊技能: 毒液 (被动)");
                                            }

                                            // ===== 添加可激活技能 =====
                                            CorpseOrigin.LOGGER.info("----- 添加可激活技能 -----");

                                            // 力量分支可激活技能
                                            if (!skillHandler.hasLearned(CorpseSkills.BERSERK.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.BERSERK);
                                                CorpseOrigin.LOGGER.info("✓ 添加可激活技能: 狂暴 (可激活)");
                                            }

                                            // 特殊分支可激活技能
                                            if (!skillHandler.hasLearned(CorpseSkills.FEAR_AURA.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.FEAR_AURA);
                                                CorpseOrigin.LOGGER.info("✓ 添加可激活技能: 恐惧光环 (可激活)");
                                            }

                                            // 终极可激活技能
                                            if (!skillHandler.hasLearned(CorpseSkills.CORPSE_KING_POWER.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.CORPSE_KING_POWER);
                                                CorpseOrigin.LOGGER.info("✓ 添加可激活技能: 尸王之力 (可激活)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.SHADOW_STRIKE.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.SHADOW_STRIKE);
                                                CorpseOrigin.LOGGER.info("✓ 添加可激活技能: 影袭 (可激活)");
                                            }

                                            if (!skillHandler.hasLearned(CorpseSkills.IMMORTAL_BODY.getId())) {
                                                skillHandler.learnSkill(CorpseSkills.IMMORTAL_BODY);
                                                CorpseOrigin.LOGGER.info("✓ 添加可激活技能: 不死之身 (可激活)");
                                            }

                                            // 给一些进化点
                                            skillHandler.addEvolutionPoints(50);

                                            // 同步到客户端
                                            skillHandler.syncToClient();

                                            // 获取最终可激活技能数量
                                            int activatableCount = 0;
                                            StringBuilder activatableSkills = new StringBuilder();
                                            for (var skill : skillHandler.getLearnedSkills()) {
                                                if (skill.isActivatable()) {
                                                    activatableCount++;
                                                    activatableSkills.append("\n  §b- ").append(skill.getName().getString())
                                                            .append(" §7(ID: ").append(skill.getId().getPath()).append(")");
                                                }
                                            }

                                            CorpseOrigin.LOGGER.info("===== 测试完成 =====");
                                            CorpseOrigin.LOGGER.info("总学习技能数: {}", skillHandler.getLearnedSkills().size());
                                            CorpseOrigin.LOGGER.info("可激活技能数: {}", activatableCount);

                                            // 发送成功消息
                                            String message = String.format(
                                                    "§a===== 可激活技能测试完成 =====\n" +
                                                            "§e已学习技能总数: §f%d 个\n" +
                                                            "§e可激活技能数量: §f%d 个\n" +
                                                            "§a已添加的可激活技能:%s\n" +
                                                            "§7按 §eR§7 键打开技能轮盘查看",
                                                    skillHandler.getLearnedSkills().size(),
                                                    activatableCount,
                                                    activatableSkills.toString()
                                            );

                                            context.getSource().sendSuccess(
                                                    () -> net.minecraft.network.chat.Component.literal(message), true
                                            );

                                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                    "§a你获得了所有可激活技能！按 R 键打开技能轮盘查看！"
                                            ));

                                            return 1;
                                        })
                                )
                        )
                        // 查看技能详情 - 添加在 testactivatable 命令后面
                        .then(Commands.literal("skillinfo")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            if (!PlayerCorpseData.isCorpse(player)) {
                                                context.getSource().sendFailure(
                                                        net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                );
                                                return 0;
                                            }

                                            ISkillHandler skillHandler = SkillAttachment.getSkillHandler(player);

                                            StringBuilder info = new StringBuilder();
                                            info.append("§6===== 玩家 ").append(player.getName().getString()).append(" 技能详情 =====\n");
                                            info.append("§e进化点: §f").append(skillHandler.getEvolutionPoints()).append("\n");

                                            int total = 0;
                                            int passive = 0;
                                            int activatable = 0;

                                            for (var skill : skillHandler.getLearnedSkills()) {
                                                total++;
                                                if (skill.isPassive()) {
                                                    passive++;
                                                } else {
                                                    activatable++;
                                                }

                                                info.append(String.format("§7[%s] §b%s §7- %s\n",
                                                        skill.isPassive() ? "被动" : "主动",
                                                        skill.getName().getString(),
                                                        skill.getId().getPath()
                                                ));
                                            }

                                            info.append("§e总计: §f").append(total).append(" 个技能 §7(被动: ").append(passive)
                                                    .append(", 可激活: ").append(activatable).append(")");

                                            context.getSource().sendSuccess(
                                                    () -> net.minecraft.network.chat.Component.literal(info.toString()), false
                                            );

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
                                            // 发送给所有在线玩家
                                            PacketDistributor.sendToAllPlayers(packet);

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
                                            boolean isGreedy = PlayerCorpseData.isGreedy(player);
                                            int variant = PlayerCorpseData.getVariant(player);
                                            boolean hasConsciousness = PlayerCorpseData.hasConsciousness(player);
                                            boolean isMindless = PlayerCorpseData.isMindless(player);
                                            boolean isRestored = PlayerCorpseData.isConsciousnessRestored(player);

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
                                                                    "§e拥有意识: §f" + hasConsciousness + (isMindless ? " §c(失去意识)" : " §a(有意识)") + "\n" +
                                                                    "§e意识恢复方式: §f" + (isRestored ? "§a进化/道具恢复" : (hasConsciousness ? "§b天生保留" : "§c无")) + "\n" +
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

                        // 恢复意识
                        .then(Commands.literal("restoreconsciousness")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");

                                            if (!PlayerCorpseData.isCorpse(player)) {
                                                context.getSource().sendFailure(
                                                        net.minecraft.network.chat.Component.literal("该玩家不是尸兄")
                                                );
                                                return 0;
                                            }

                                            if (PlayerCorpseData.hasConsciousness(player)) {
                                                context.getSource().sendFailure(
                                                        net.minecraft.network.chat.Component.literal("该玩家已经拥有意识")
                                                );
                                                return 0;
                                            }

                                            PlayerCorpseData.restoreConsciousness(player);

                                            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                                                    "§a§l你的意识被强制恢复！§r\n" +
                                                    "§7你现在可以使用工作台、门等复杂物品了。"
                                            ));

                                            context.getSource().sendSuccess(
                                                    () -> net.minecraft.network.chat.Component.literal(
                                                            "已恢复玩家 " + player.getName().getString() + " 的意识"
                                                    ), true
                                            );

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