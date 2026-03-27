package com.phagens.corpseorigin.client.Models.entity;

import com.phagens.corpseorigin.entity.GuigunEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class GuigunModel extends GeoModel<GuigunEntity> {
    @Override
    public ResourceLocation getModelResource(GuigunEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"geo/entity/guigun.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(GuigunEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"textures/entity/guigun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(GuigunEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(MODID,"animations/entity/guigun.animation.json");
    }
}