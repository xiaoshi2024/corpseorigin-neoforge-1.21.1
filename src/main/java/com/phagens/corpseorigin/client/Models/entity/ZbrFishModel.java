package com.phagens.corpseorigin.client.Models.entity;

import com.phagens.corpseorigin.Entity.ZbrFishEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class ZbrFishModel extends DefaultedEntityGeoModel<ZbrFishEntity> {
    public ZbrFishModel() {
        super(ResourceLocation.fromNamespaceAndPath(MODID, "zbr_fish"));
    }

    @Override
    public ResourceLocation getModelResource(ZbrFishEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "geo/entity/zbr_fish.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ZbrFishEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/zbr_fish.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ZbrFishEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID, "animations/entity/zbr_fish.animation.json");
    }
}