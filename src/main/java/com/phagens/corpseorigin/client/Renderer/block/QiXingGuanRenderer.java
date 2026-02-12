package com.phagens.corpseorigin.client.Renderer.block;

import com.phagens.corpseorigin.Block.entity.QiXingGuanBlockEntity;
import com.phagens.corpseorigin.client.Models.block.QiXingGuanModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class QiXingGuanRenderer extends GeoBlockRenderer<QiXingGuanBlockEntity> {
    public QiXingGuanRenderer(BlockEntityRendererProvider.Context context) {
        super(new QiXingGuanModel());
    }

}
