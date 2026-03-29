package com.phagens.corpseorigin.client.Models.item;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.Organic.DrMuEyeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

/**
 * 穆博士眼睛的模型
 */
public class DrMuEyeModel extends GeoModel<DrMuEyeItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "geo/item/dr_mu_eye.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/item/dr_mu_eye.png");
    private static final ResourceLocation ANIMATION = ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "animations/item/dr_mu_eye.animation.json");

    @Override
    public ResourceLocation getModelResource(DrMuEyeItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DrMuEyeItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DrMuEyeItem animatable) {
        return ANIMATION;
    }
}
