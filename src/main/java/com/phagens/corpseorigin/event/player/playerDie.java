package com.phagens.corpseorigin.event.player;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.GongFU.GongFaZL.BaseGongFaItem;
import com.phagens.corpseorigin.GongFU.GongFaZL.GongFaData;
import com.phagens.corpseorigin.GongFU.GongFaZL.GongFaSkillManager;
import com.phagens.corpseorigin.GongFU.ModUtlis.GongFUDataUtlis;
import com.phagens.corpseorigin.Item.YaoJi.Sagent;
import com.phagens.corpseorigin.skill.ISkillHandler;
import com.phagens.corpseorigin.skill.SkillAttachment;
import com.phagens.corpseorigin.skill.SkillHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.phagens.corpseorigin.GongFU.ModUtlis.GongFUDataUtlis.applyGongFaAttributes;

//兼容修行
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class playerDie {
    // 服务器级别的内存缓存
    private static final Map<UUID, CompoundTag> DEATH_BACKUP = new ConcurrentHashMap<>();
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {

            CompoundTag playerData = player.getPersistentData();
            CompoundTag gongFuData = playerData.getCompound("GongFuContainer");
            if (!gongFuData.isEmpty()) {
                // 保存到服务器内存缓存
                DEATH_BACKUP.put(player.getUUID(), gongFuData.copy());
            }

            // 玩家死亡时移除所有CorpseOrigin添加的属性修饰符
            Sagent.removeAllPlayerAttributes(player);
        }
    }
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event){
        Player original = event.getOriginal();  // 死亡前的玩家实体
        Player newPlayer = event.getEntity();   // 重生后的玩家实体
        // 玩家重生时恢复数据
        UUID playerUUID = original.getUUID();
        if (DEATH_BACKUP.containsKey(playerUUID)) {
            CompoundTag backupData = DEATH_BACKUP.get(playerUUID);
            newPlayer.getPersistentData().put("GongFuContainer", backupData);
            DEATH_BACKUP.remove(playerUUID); // 清理缓存

        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            applyGongFaAttributes(player);
            if (player.tickCount % 100 == 0) {
                checkAndLearnGongFuSkills(player);
            }
        }
    }

    private static void checkAndLearnGongFuSkills(Player player) {
        // 获取玩家的技能处理器
        ISkillHandler handler = SkillAttachment.getSkillHandler(player);
        if (handler == null) {
            return;  // 没有技能处理器（可能是普通人）
        }

        // 获取修行容器中的所有物品
        NonNullList<ItemStack> gongFuItems = GongFUDataUtlis.getGongFuItems(player);

        for (ItemStack stack : gongFuItems) {
            // 检查是否是功法物品
            if (!stack.isEmpty() && stack.getItem() instanceof BaseGongFaItem gongFaItem) {
                // 从物品中读取功法数据
                GongFaData data = gongFaItem.getDataFromItem(stack);
                if (data == null) {
                    continue;  // 没有有效的功法数据
                }

                // 获取对应的技能 ID
                ResourceLocation skillId = GongFaSkillManager.getInstance()
                        .getGongFuSkillId(data.getTypeId(), data.getRarity(), data.getCeng());

                if (skillId == null) {
                    CorpseOrigin.LOGGER.warn("未找到功法技能 ID: {}_{}_{}",
                            data.getTypeId(), data.getRarity(), data.getCeng());
                    continue;  // 技能未注册
                }

                // 检查是否已经学习了该技能
                if (!handler.hasLearned(skillId)) {
                    // 尝试学习技能
                    var result = ((SkillHandler) handler).learnGongFuSkill(skillId);

                    if (result == ISkillHandler.LearnResult.SUCCESS ||
                            result == ISkillHandler.LearnResult.ALREADY_LEARNED) {
                        // 发送提示消息给玩家
                        String skillNames = String.join(", ", data.getSkills());
                        player.sendSystemMessage(Component.literal(
                                "§a§l✦ 习得功法技艺：§r§6" + skillNames +
                                        " §7(§e" + data.getName() + "§7)"
                        ));

                        CorpseOrigin.LOGGER.info("玩家 {} 自动习得功法技能：{} -> {}",
                                player.getName().getString(), data.getTypeId(), skillId);
                    } else {
                        CorpseOrigin.LOGGER.warn("玩家 {} 学习功法技能失败：{} - {}",
                                player.getName().getString(), data.getTypeId(), result);
                    }
                }
            }
        }
    }

}



