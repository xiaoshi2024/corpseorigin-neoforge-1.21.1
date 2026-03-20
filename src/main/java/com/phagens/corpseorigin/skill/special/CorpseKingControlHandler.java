package com.phagens.corpseorigin.skill.special;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.UUID;

/**
 * 尸王控制处理器 - 处理被控制玩家的行为
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class CorpseKingControlHandler {

    /**
     * 玩家tick事件 - 处理被控制玩家的强制行为
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) return;

        // 更新控制状态
        if (player.level() instanceof ServerLevel level) {
            CorpseKingPowerSkill.updateControls(level);
        }

        // 检查玩家是否被控制
        if (CorpseKingPowerSkill.isPlayerControlled(player)) {
            handleControlledPlayer(player);
        }
    }

    /**
     * 处理被控制玩家的行为
     */
    private static void handleControlledPlayer(Player controlled) {
        // 获取控制者的UUID
        UUID controllerUUID = CorpseKingPowerSkill.getControllerUUID(controlled);
        if (controllerUUID == null) return;

        // 获取控制者实体
        if (!(controlled.level() instanceof ServerLevel level)) return;
        Player controller = level.getServer().getPlayerList().getPlayer(controllerUUID);
        if (controller == null) return;

        // 强制被控制玩家看向控制者看向的方向
        Vec3 controllerLook = controller.getLookAngle();
        double yaw = Math.toDegrees(Math.atan2(controllerLook.x, controllerLook.z));
        double pitch = Math.toDegrees(Math.asin(-controllerLook.y));

        controlled.setYRot((float) yaw);
        controlled.setXRot((float) pitch);
        controlled.setYHeadRot((float) yaw);

        // 如果被定身，强制停止移动
        if (CorpseKingPowerSkill.isPlayerFrozen(controlled)) {
            controlled.setDeltaMovement(0, controlled.getDeltaMovement().y, 0);
        }
    }

    /**
     * 检查玩家是否可以自主行动
     */
    public static boolean canPlayerActFreely(Player player) {
        return !CorpseKingPowerSkill.isPlayerControlled(player);
    }
}
