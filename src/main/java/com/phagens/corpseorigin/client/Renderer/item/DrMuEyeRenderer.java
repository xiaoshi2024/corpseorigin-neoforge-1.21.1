package com.phagens.corpseorigin.client.Renderer.item;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Item.Organic.DrMuEyeItem;
import com.phagens.corpseorigin.client.Models.item.DrMuEyeModel;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * 穆博士眼睛的渲染器
 */
public class DrMuEyeRenderer extends GeoItemRenderer<DrMuEyeItem> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, "textures/item/dr_mu_eye.png");

    public DrMuEyeRenderer() {
        super(new DrMuEyeModel());
    }

    @Override
    public ResourceLocation getTextureLocation(DrMuEyeItem animatable) {
        return TEXTURE;
    }
}
