package com.phagens.corpseorigin.client.Models.block;

import com.phagens.corpseorigin.Block.entity.QiXingGuanBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class QiXingGuanModel extends DefaultedBlockGeoModel<QiXingGuanBlockEntity> {

    // 显式定义纹理路径
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MODID, "textures/block/qi_xing_guan.png");

    public QiXingGuanModel() {
        super(ResourceLocation.fromNamespaceAndPath(MODID, "qi_xing_guan"));
    }

    @Override
    public ResourceLocation getTextureResource(QiXingGuanBlockEntity animatable) {
        return TEXTURE;
    }

    @Override
    public RenderType getRenderType(QiXingGuanBlockEntity animatable, ResourceLocation texture) {
        // 使用 entityCutoutNoCull 确保正确渲染，或使用 translucent 如果需要透明
        return RenderType.entityCutoutNoCull(texture);
    }
}