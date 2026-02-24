package com.phagens.corpseorigin.event.player;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.YaoJi.YAOJIMoBan;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class playerDie {
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            // 玩家死亡时移除所有CorpseOrigin添加的属性修饰符
            YAOJIMoBan.removeAllPlayerAttributes(player);
        }
    }
}
