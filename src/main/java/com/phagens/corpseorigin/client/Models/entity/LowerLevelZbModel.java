package com.phagens.corpseorigin.client.Models.entity;

import com.phagens.corpseorigin.entity.LowerLevelZbEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class LowerLevelZbModel extends GeoModel<LowerLevelZbEntity> {
    // 使用 HumanoidModel 作为基础，但修改为 slim 手臂
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // 修改手臂为 slim 版本 (3x12x4 而不是 4x12x4)
        partdefinition.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F), // slim 手臂宽度为3
                PartPose.offset(-5.0F, 2.0F, 0.0F));

        partdefinition.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16)
                        .mirror()
                        .addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F), // slim 手臂宽度为3
                PartPose.offset(5.0F, 2.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    // 其余方法保持不变...
    @Override
    public ResourceLocation getModelResource(LowerLevelZbEntity object) {
        switch (object.getVariant()) {
            case CRACKED:
                return ResourceLocation.fromNamespaceAndPath(MODID, "geo/entity/lower_level_zb_rendering.geo.json");
            default:
                return ResourceLocation.fromNamespaceAndPath(MODID, "geo/entity/lower_level_zb.geo.json");
        }
    }

    @Override
    public ResourceLocation getTextureResource(LowerLevelZbEntity object) {
        switch (object.getVariant()) {
            case CRACKED:
                return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/lower_level_zb_rendering.png");
            default:
                return ResourceLocation.fromNamespaceAndPath(MODID, "textures/entity/lower_level_zb_render.png");
        }
    }

    @Override
    public ResourceLocation getAnimationResource(LowerLevelZbEntity animatable) {
        switch (animatable.getVariant()) {
            case CRACKED:
                return ResourceLocation.fromNamespaceAndPath(MODID, "animations/entity/lower_level_zb_rendering.animation.json");
            default:
                return ResourceLocation.fromNamespaceAndPath(MODID, "animations/entity/lower_level_zb.animation.json");
        }
    }
}