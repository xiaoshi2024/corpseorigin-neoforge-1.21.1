package com.phagens.corpseorigin.client.Renderer.item;

import com.phagens.corpseorigin.Item.JuQue;
import com.phagens.corpseorigin.client.Models.item.JuQueModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class JuQueRenderer extends GeoItemRenderer<JuQue> {
    public JuQueRenderer() {
        super(new JuQueModel());
    }
}
