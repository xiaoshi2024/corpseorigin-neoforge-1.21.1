package com.phagens.corpseorigin.client.Renderer.entity;

import com.phagens.corpseorigin.Entity.ZbrFishEntity;
import com.phagens.corpseorigin.client.Models.entity.ZbrFishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ZbrFishRenderer extends GeoEntityRenderer<ZbrFishEntity> {
    public ZbrFishRenderer(EntityRendererProvider.Context context) {
        super(context, new ZbrFishModel());
    }
}