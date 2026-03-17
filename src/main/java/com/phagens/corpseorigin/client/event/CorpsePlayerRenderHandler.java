package com.phagens.corpseorigin.client.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.client.Renderer.layer.ExoskeletonRenderLayer;
import com.phagens.corpseorigin.client.model.CorpsemoldelRegister;
import com.phagens.corpseorigin.client.model.ExoskeletonModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 尸兄玩家渲染处理器
 * 负责在玩家成为尸兄时添加外骨骼渲染层
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class CorpsePlayerRenderHandler {

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        CorpseOrigin.LOGGER.info("Adding exoskeleton layer to player renderer");

        EntityModelSet modelSet = event.getContext().getModelSet();

        // 为默认玩家渲染器添加外骨骼层
        PlayerRenderer defaultRenderer = event.getSkin(PlayerSkin.Model.WIDE);
        addExoskeletonLayer(defaultRenderer, modelSet);

        // 为纤细模型玩家渲染器添加外骨骼层
        PlayerRenderer slimRenderer = event.getSkin(PlayerSkin.Model.SLIM);
        addExoskeletonLayer(slimRenderer, modelSet);
    }

    private static void addExoskeletonLayer(PlayerRenderer renderer, EntityModelSet modelSet) {
        if (renderer == null) return;

        ExoskeletonModel model = new ExoskeletonModel(
                modelSet.bakeLayer(CorpsemoldelRegister.EXOSKELETON_LAYER)
        );

        renderer.addLayer(new ExoskeletonRenderLayer(renderer, model));
    }
}
