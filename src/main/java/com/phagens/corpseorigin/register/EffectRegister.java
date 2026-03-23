package com.phagens.corpseorigin.register;

import com.phagens.corpseorigin.CorpseOrigin;
import com.phagens.corpseorigin.effect.BYeffect;
import com.phagens.corpseorigin.effect.SideEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectRegister {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, CorpseOrigin.MODID);


    public static final Holder<MobEffect> QIANS = MOB_EFFECTS.register("by", () -> new BYeffect(
            MobEffectCategory.HARMFUL,
            0xffffff
    ));

    // 黄色强化剂副作用效果
    public static final Holder<MobEffect> SIDE_EFFECT = MOB_EFFECTS.register("side_effect", () -> new SideEffect(
            MobEffectCategory.HARMFUL,
            0xFFAA00 // 橙黄色，表示危险
    ));


}
