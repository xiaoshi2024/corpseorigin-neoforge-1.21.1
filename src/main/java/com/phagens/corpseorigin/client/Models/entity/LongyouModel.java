package com.phagens.corpseorigin.client.Models.entity;

import com.phagens.corpseorigin.Entity.LongyouEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class LongyouModel extends GeoModel<LongyouEntity> {
    @Override
    public ResourceLocation getModelResource(LongyouEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"geo/entity/longyou.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(LongyouEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"textures/entity/longyou.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LongyouEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"animations/entity/longyou.animation.json");
    }
}
