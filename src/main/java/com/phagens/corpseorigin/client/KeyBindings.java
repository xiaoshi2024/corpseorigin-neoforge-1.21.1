package com.phagens.corpseorigin.client;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.gui.SkillRadialScreen;
import com.phagens.corpseorigin.client.gui.SkillTreeScreen;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键绑定
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class KeyBindings {
    
    // 技能轮盘按键
    public static final KeyMapping SKILL_WHEEL = new KeyMapping(
            "key.corpseorigin.skill_wheel",
            GLFW.GLFW_KEY_R,
            "key.categories.corpseorigin"
    );
    
    // 技能树界面按键
    public static final KeyMapping SKILL_TREE = new KeyMapping(
            "key.corpseorigin.skill_tree",
            GLFW.GLFW_KEY_K,
            "key.categories.corpseorigin"
    );
    
    /**
     * 注册按键绑定
     */
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(SKILL_WHEEL);
        event.register(SKILL_TREE);
    }
    
    /**
     * 客户端 tick 事件 - 处理按键
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null) {
            return;
        }

        // 检查是否是尸兄
        boolean isCorpse = PlayerCorpseData.isCorpse(minecraft.player);

        if (SKILL_WHEEL.consumeClick()) {
            openSkillWheel();
            // Deleted: if (isCorpse) {
            // Deleted:     openSkillWheel();
            // Deleted: } else {
            // Deleted:     showOrdinaryPlayerMessage(minecraft);
            // Deleted: }
        }

        // 技能轮盘按键
        if (SKILL_WHEEL.consumeClick()) {
            if (isCorpse) {
                openSkillWheel();
            } else {
                showOrdinaryPlayerMessage(minecraft);
            }
        }

        // 技能树按键
        if (SKILL_TREE.consumeClick()) {
            if (isCorpse) {
                openSkillTree();
            } else {
                showOrdinaryPlayerMessage(minecraft);
            }
        }
    }

    /**
     * 显示普通玩家提示信息
     */
    private static void showOrdinaryPlayerMessage(Minecraft minecraft) {
        if (minecraft.player != null) {
            minecraft.player.sendSystemMessage(Component.translatable("message.corpseorigin.ordinary_player"));
        }
    }
    
    /**
     * 打开技能轮盘
     */
    private static void openSkillWheel() {
        Minecraft minecraft = Minecraft.getInstance();
        
        if (minecraft.screen == null && minecraft.player != null) {
            SkillRadialScreen.show();
        }
    }
    
    /**
     * 打开技能树界面
     */
    private static void openSkillTree() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.screen == null && minecraft.player != null) {
            SkillTreeScreen.show();
        }
    }
}
