package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.entity.LowerLevelZbEntity;
import com.phagens.corpseorigin.entity.LongyouEntity;
import com.phagens.corpseorigin.entity.ZbrFishEntity;
import com.phagens.corpseorigin.entity.npc.KaiWeiNaiEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.phagens.corpseorigin.CorpseOrigin.MODID;

public class EntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ZbrFishEntity>> ZBR_FISH = ENTITIES.register("zbr_fish",
            () -> EntityType.Builder.<ZbrFishEntity>of(ZbrFishEntity::new, MobCategory.MONSTER)
                    .sized(0.5F, 0.5F)
                    .build("zbr_fish"));

    public static final DeferredHolder<EntityType<?>, EntityType<LowerLevelZbEntity>> LOWER_LEVEL_ZB = ENTITIES.register("lower_level_zb",
            () -> EntityType.Builder.<LowerLevelZbEntity>of(LowerLevelZbEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.8F)
                    .build("lower_level_zb"));

    public static final DeferredHolder<EntityType<?>, EntityType<LongyouEntity>> LONGYOU = ENTITIES.register("longyou",
            () -> EntityType.Builder.<LongyouEntity>of(LongyouEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.5F)
                    .build("longyou"));

    // 开胃奶NPC
    public static final DeferredHolder<EntityType<?>, EntityType<KaiWeiNaiEntity>> KAIWEINAI = ENTITIES.register("kaiweinai",
            () -> EntityType.Builder.<KaiWeiNaiEntity>of(KaiWeiNaiEntity::new, MobCategory.CREATURE)
                    .sized(0.6F, 1.8F)
                    .build("kaiweinai"));
}