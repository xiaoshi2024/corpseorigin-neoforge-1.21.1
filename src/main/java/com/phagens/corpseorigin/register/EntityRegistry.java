package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.Entity.ZbrFishEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZbrFishEntity>> ZBR_FISH = ENTITIES.register("zbr_fish",
            () -> EntityType.Builder.of(ZbrFishEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.5F)
                    .build("zbr_fish"));
}