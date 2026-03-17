package com.phagens.corpseorigin.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.network.PlayerCorpseSyncPacket;
import com.phagens.corpseorigin.player.PlayerCorpseData;
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
                                
                                // 同步到客户端
                                PlayerCorpseSyncPacket packet = new PlayerCorpseSyncPacket(
                                    player.getId(), true, type, PlayerCorpseData.getCorpseData(player)
                                );
                                PacketDistributor.sendToPlayer(player, packet);
                                PacketDistributor.sendToPlayersTrackingEntity(player, packet);
                                
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
                                    "  变种: " + (variant == 1 ? "裂口" : "普通")
                                ), false
                            );
                            return 1;
                        })
                    )
                )
        );
    }
}
