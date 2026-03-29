package com.phagens.corpseorigin.client.Renderer.item;

import com.phagens.corpseorigin.Item.Organic.OrdinaryZbEyeItem;
import com.phagens.corpseorigin.client.Models.item.OrdinaryZbEyeModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class OrdinaryZbEyeRenderer extends GeoItemRenderer<OrdinaryZbEyeItem> {
    public OrdinaryZbEyeRenderer() {
        super(new OrdinaryZbEyeModel());
    }
}
