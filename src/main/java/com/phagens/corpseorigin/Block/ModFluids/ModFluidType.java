package com.phagens.corpseorigin.Block.ModFluids;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.joml.Vector3f;

import java.lang.reflect.Method;

public class ModFluidType extends FluidType {

    private final ResourceLocation stillTexture;
    private final ResourceLocation flowTexture;
    private final int tintColer;
    private final Vector3f fogColor;

    public ModFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowTexture, int tintColer, Vector3f fogColor) {
        super(properties);
        this.stillTexture = stillTexture;
        this.flowTexture = flowTexture;
        this.tintColer = tintColer;
        this.fogColor = fogColor;
    }

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, CorpseOrigin.MODID);

    public static final DeferredHolder<FluidType, FluidType> SOUREC_BYWATER = FLUID_TYPES.register("source_bywater",
            () -> new ModFluidType(FluidType.Properties.create(),ResourceLocation.parse("minecraft:block/water_still"),
                    ResourceLocation.parse("minecraft:block/water_flow"),0xFF0000,new Vector3f(0.5f,0.5f,0.5f)));

    @Override
    public void setItemMovement(ItemEntity entity) {
        super.setItemMovement(entity);
    }






    @EventBusSubscriber(modid = CorpseOrigin.MODID, value = Dist.CLIENT)
    public static class ModJT {
        @SubscribeEvent  //
        public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
            FluidType fluid = ModFluidType.SOUREC_BYWATER.get();
            event.registerFluidType(
                    new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {return ResourceLocation.parse("minecraft:block/water_still");}
                        @Override
                        public ResourceLocation getFlowingTexture() {return ResourceLocation.parse("minecraft:block/water_flow");}
                        @Override
                        public int getTintColor() {return  0xFF0000;}
                    },fluid);
        }
    }


}
      
