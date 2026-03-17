package com.phagens.corpseorigin.client.model;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * 尸化外骨骼模型层注册
 */
@EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
public class CorpsemoldelRegister {

    // 外骨骼模型层位置
    public static final ModelLayerLocation EXOSKELETON_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "exoskeleton"), "main");

    /**
     * 注册模型层定义
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册外骨骼模型层
        event.registerLayerDefinition(EXOSKELETON_LAYER, ExoskeletonLayerDefinitions::createExoskeleton);

        CorpseOrigin.LOGGER.info("Registered exoskeleton layer definition");
    }
}
