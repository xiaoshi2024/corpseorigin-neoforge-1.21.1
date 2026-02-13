package com.phagens.corpseorigin.Block.ModFluids;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Modfluid {

    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, CorpseOrigin.MODID);


    public static final DeferredHolder<Fluid, ByWaterZL.FlowingBY> FLOWING_BYWATER =
            FLUIDS.register("flowing_bywater", ByWaterZL.FlowingBY::new);

    public static final DeferredHolder<Fluid, ByWaterZL.SourceBY> SOUREC_BYWATER =
            FLUIDS.register("source_bywater", ByWaterZL.SourceBY::new);

    public static void init(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        CorpseOrigin.LOGGER.info("Registered fluids: flowing_bywater, source_bywater");
    }




}
