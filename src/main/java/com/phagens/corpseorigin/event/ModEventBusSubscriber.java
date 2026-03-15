package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Entity.LongyouEntity;
import com.phagens.corpseorigin.Entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.Entity.ZbrFishEntity;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class ModEventBusSubscriber {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.ZBR_FISH.get(), ZbrFishEntity.createAttributes().build());
        event.put(EntityRegistry.LOWER_LEVEL_ZB.get(), LowerLevelZbEntity.createAttributes().build());
        event.put(EntityRegistry.LONGYOU.get(), LongyouEntity.createAttributes().build());
    }
}
