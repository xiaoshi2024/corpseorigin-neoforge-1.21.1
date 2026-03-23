package com.phagens.corpseorigin.client.Models.entity;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.entity.npc.KaiWeiNaiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 开胃奶NPC的GeckoLib模型
 */
public class KaiWeiNaiModel extends GeoModel<KaiWeiNaiEntity> {

    @Override
    public ResourceLocation getModelResource(KaiWeiNaiEntity object) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "geo/entity/kaiweinai.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(KaiWeiNaiEntity object) {
        // 使用玩家皮肤或者自定义纹理
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/entity/kaiweinai.png");
    }

    @Override
    public ResourceLocation getAnimationResource(KaiWeiNaiEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "animations/entity/kaiweinai.animation.json");
    }
}
