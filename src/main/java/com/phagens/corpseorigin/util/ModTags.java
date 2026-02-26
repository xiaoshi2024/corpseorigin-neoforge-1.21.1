package com.phagens.corpseorigin.util;

import com.phagens.corpseorigin.CorpseOrigin;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class ModTags {
    public static class Fluids {
        public static final TagKey<Fluid> BYWATER = createTag("bywater");
        
        private static TagKey<Fluid> createTag(String name) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(CorpseOrigin.MODID, name));
        }
    }
}