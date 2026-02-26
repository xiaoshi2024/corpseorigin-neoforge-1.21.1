package com.phagens.corpseorigin.client.Renderer.item;

import com.phagens.corpseorigin.Item.YaoJi.Sagent;
import com.phagens.corpseorigin.client.Models.item.SagentModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SagentRenderer extends GeoItemRenderer<Sagent> {
    public SagentRenderer() {
        super(new SagentModel());
    }
}