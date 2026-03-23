package com.phagens.corpseorigin.client.Models.item;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.JuQue;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JuQueModel extends GeoModel<JuQue> {

    @Override
    public ResourceLocation getModelResource(JuQue object) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "geo/item/ming_juque.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JuQue object) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/item/ming_juque.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JuQue animatable) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "animations/item/ming_juque.animation.json");
    }
}
