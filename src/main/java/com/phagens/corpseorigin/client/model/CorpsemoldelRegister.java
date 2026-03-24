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
    
    // 羽翼模型层位置
    public static final ModelLayerLocation WING_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "corpse_wing"), "main");
    
    // 鱼尾模型层位置
    public static final ModelLayerLocation TAIL_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "corpse_tail"), "main");

    /**
     * 注册模型层定义
     */
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册外骨骼模型层
        event.registerLayerDefinition(EXOSKELETON_LAYER, ExoskeletonLayerDefinitions::createExoskeleton);
        // 注册羽翼模型层
        event.registerLayerDefinition(WING_LAYER, CorpseExoskeletonLayerDefinitions::createWingLayer);
        // 注册鱼尾模型层
        event.registerLayerDefinition(TAIL_LAYER, CorpseExoskeletonLayerDefinitions::createTailLayer);

        CorpseOrigin.LOGGER.info("Registered exoskeleton layer definitions");
    }
}
