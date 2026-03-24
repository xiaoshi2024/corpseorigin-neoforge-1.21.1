package com.phagens.corpseorigin.client.event;

import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = "corpseorigin", value = Dist.CLIENT)
public class CorpseHudEventHandler {

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        renderCorpseHungerHud(event.getGuiGraphics());
    }

    private static void renderCorpseHungerHud(GuiGraphics guiGraphics) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null) return;

        // 检查玩家是否是尸兄
        if (!PlayerCorpseData.isCorpse(player)) return;

        // 获取屏幕宽度和高度
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        // 计算渲染位置（在血条上方）
        int x = screenWidth / 2 - 91;
        int y = screenHeight - 39;

        // 获取尸兄饥饿度
        int hunger = PlayerCorpseData.getHunger(player);
        int maxHunger = 100;

        // 渲染饥饿度背景
        guiGraphics.fill(x, y - 10, x + 80, y - 1, 0x40000000);

        // 渲染饥饿度条
        int hungerBarWidth = (int) ((float) hunger / maxHunger * 80);
        guiGraphics.fill(x, y - 10, x + hungerBarWidth, y - 1, 0xFF00FF00);

        // 渲染饥饿度文本
        guiGraphics.drawString(minecraft.font, "饥饿度: " + hunger, x, y - 20, 0xFFFFFF);
    }
}