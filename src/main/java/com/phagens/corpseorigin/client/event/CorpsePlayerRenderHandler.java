package com.phagens.corpseorigin.client.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.Renderer.entity.CorpsePlayerGeoRenderer;
import com.phagens.corpseorigin.player.PlayerCorpseData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class CorpsePlayerRenderHandler {

    private static CorpsePlayerGeoRenderer corpseRenderer;

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        // 创建自定义渲染器
        corpseRenderer = new CorpsePlayerGeoRenderer(event.getContext());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();

        // 不是尸兄，使用原版渲染器
        if (!PlayerCorpseData.isCorpse(player)) {
            return;
        }

        // 取消原版渲染
        event.setCanceled(true);

        if (corpseRenderer == null) {
            CorpseOrigin.LOGGER.error("CorpsePlayerRenderer not initialized!");
            return;
        }

        // 使用自定义渲染器渲染
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;

        // 直接调用渲染方法
        corpseRenderer.render(
                clientPlayer,
                clientPlayer.getYRot(),
                event.getPartialTick(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight()
        );
    }
}