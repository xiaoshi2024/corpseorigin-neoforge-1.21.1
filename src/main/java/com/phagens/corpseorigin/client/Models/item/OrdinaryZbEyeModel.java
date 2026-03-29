package com.phagens.corpseorigin.client.Models.item;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.Organic.OrdinaryZbEyeItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class OrdinaryZbEyeModel extends GeoModel<OrdinaryZbEyeItem> {
    
    @Override
    public ResourceLocation getModelResource(OrdinaryZbEyeItem object) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "geo/item/organic/ordinary_zb_eye.geo.json");
    }
    
    @Override
    public ResourceLocation getTextureResource(OrdinaryZbEyeItem object) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/item/ordinary_zb_eye.png");
    }
    
    @Override
    public ResourceLocation getAnimationResource(OrdinaryZbEyeItem animatable) {
        return ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "animations/item/organic/ordinary_zb_eye.animation.json");
    }
}
