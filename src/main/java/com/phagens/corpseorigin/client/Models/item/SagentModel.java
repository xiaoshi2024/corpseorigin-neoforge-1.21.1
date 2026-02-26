package com.phagens.corpseorigin.client.Models.item;

import com.phagens.corpseorigin.Item.YaoJi.Sagent;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedItemGeoModel;

public class SagentModel extends DefaultedItemGeoModel<Sagent> {
    public SagentModel() {
        super(ResourceLocation.fromNamespaceAndPath("corpseorigin", "s_agent"));
    }

    @Override
    public ResourceLocation getTextureResource(Sagent item) {
        String variant = item.getVariant();
        return ResourceLocation.fromNamespaceAndPath("corpseorigin", "textures/item/" + variant + "_s_agent.png");
    }
}