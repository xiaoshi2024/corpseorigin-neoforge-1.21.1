package com.phagens.corpseorigin.event;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.Entity.ZbrFishEntity;
import com.phagens.corpseorigin.register.EntityRegistry;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

@EventBusSubscriber(modid = CorpseOrigin.MODID)
public class ModEventBusSubscriber {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegistry.ZBR_FISH.get(), ZbrFishEntity.createAttributes().build());
    }
}
